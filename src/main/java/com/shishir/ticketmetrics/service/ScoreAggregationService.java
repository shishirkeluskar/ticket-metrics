package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.model.RatingWithCategory;
import com.shishir.ticketmetrics.model.TimeBucket;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.util.TimeBucketResolver;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreAggregationService {
  private final RatingDao ratingDao;
  
  public ScoreAggregationService(RatingDao ratingDao) {
    this.ratingDao = ratingDao;
  }
  
  /**
   * Calculates average percentage scores for each rating category within a time period.
   *
   * <p>Each rating contributes to its category's score for the day/week it was created.
   * For longer periods (> 1 month), scores are bucketed by week; otherwise by day.
   *
   * <p><b>Important:</b> Aggregation is based on <code>ratings.created_at</code>,
   * not <code>tickets.created_at</code>, because feedback is timestamped at rating time.
   *
   * <p>Example 1 (Daily):
   * <ul>
   *   <li>Rating A: 4 stars on 2025-07-01 → contributes to July 1</li>
   *   <li>Rating B: 2 stars on 2025-07-01 → also July 1</li>
   * </ul>
   * → Result: (4 + 2) / (5 + 5) = 60%
   *
   * <p>Example 2 (Weekly):
   * <ul>
   *   <li>Rating C: 5 stars on 2025-07-02</li>
   *   <li>Rating D: 0 stars on 2025-07-05</li>
   * </ul>
   * → Result for Week 1 of July: (5 + 0) / (5 + 5) = 50%
   *
   * @param startDate start of time range (inclusive)
   * @param endDate   end of time range (inclusive)
   * @return map from category ID to summary of score, timeline and count
   */
  public Map<Integer, CategoryScoreSummary> getCategoryScoresOverTime(LocalDateTime startDate, LocalDateTime endDate) {
    TimeBucket bucket = TimeBucketResolver.resolve(startDate, endDate);
    
    List<RatingWithCategory> ratings = ratingDao.findRatingsInRange(startDate, endDate);
    
    Map<Integer, Map<LocalDateTime, List<BigDecimal>>> grouped = new LinkedHashMap<>();
    
    for (RatingWithCategory row : ratings) {
      int categoryId = row.categoryId();
      LocalDateTime bucketDate = toBucketDate(row.timestamp(), bucket);
      BigDecimal rating = row.rating();
      
      grouped
          .computeIfAbsent(categoryId, k -> new LinkedHashMap<>())
          .computeIfAbsent(bucketDate, d -> new ArrayList<>())
          .add(rating);
    }
    
    Map<Integer, CategoryScoreSummary> result = new LinkedHashMap<>();
    
    for (Map.Entry<Integer, Map<LocalDateTime, List<BigDecimal>>> categoryEntry : grouped.entrySet()) {
      int categoryId = categoryEntry.getKey();
      CategoryScoreSummary summary = new CategoryScoreSummary();
      
      for (Map.Entry<LocalDateTime, List<BigDecimal>> dateEntry : categoryEntry.getValue().entrySet()) {
        List<BigDecimal> scores = dateEntry.getValue();
        BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 6, RoundingMode.HALF_UP);
        BigDecimal percent = avg.multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
        summary.addScore(dateEntry.getKey(), percent, scores.size());
      }
      
      result.put(categoryId, summary);
    }
    
    return result;
  }
  
  private LocalDateTime toBucketDate(LocalDateTime ts, TimeBucket bucket) {
    if (bucket == TimeBucket.DAILY) {
      return ts.toLocalDate().atStartOfDay();
    } else {
      // Truncate to Monday of the week
      return ts.toLocalDate()
          .with(DayOfWeek.MONDAY)
          .atStartOfDay();
    }
  }
}

