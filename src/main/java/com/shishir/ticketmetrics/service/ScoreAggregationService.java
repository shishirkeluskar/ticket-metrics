package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.model.RatingWithCategory;
import com.shishir.ticketmetrics.model.TimeBucket;
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
  private final RatingMapper ratingMapper;
  
  public ScoreAggregationService(RatingMapper ratingMapper) {
    this.ratingMapper = ratingMapper;
  }
  
  public Map<Integer, CategoryScoreSummary> getCategoryScores(LocalDateTime startDate, LocalDateTime endDate) {
    TimeBucket bucket = TimeBucketResolver.resolve(startDate, endDate);
    
    List<RatingWithCategory> ratings = ratingMapper.findRatingsInRange(startDate, endDate);
    
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
        BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP);
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

