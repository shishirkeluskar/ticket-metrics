package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.model.RatingWithCategory;
import com.shishir.ticketmetrics.model.RatingWithCategoryWeight;
import com.shishir.ticketmetrics.model.TimeBucket;
import com.shishir.ticketmetrics.util.TimeBucketResolver;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ScoreAggregationService {
  private final RatingMapper ratingMapper;
  
  public ScoreAggregationService(RatingMapper ratingMapper) {
    this.ratingMapper = ratingMapper;
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
  
  @Cacheable(value = "ticketCategoryScores", key = "#ticketId + '-' + #categoryId + '-' + #start.toString() + '-' + #end.toString()")
  public BigDecimal calculateScoreForTicketCategory(int ticketId, int categoryId, LocalDateTime start, LocalDateTime end) {
    // Call mapper to get ratings for this ticket-category in period
    List<RatingWithCategoryWeight> ratings = ratingMapper.findRatingsForTicketCategoryBetween(ticketId, categoryId, start, end);
    
    BigDecimal totalWeight = BigDecimal.ZERO;
    BigDecimal weightedSum = BigDecimal.ZERO;
    
    for (RatingWithCategoryWeight r : ratings) {
      BigDecimal rating = r.rating();
      BigDecimal weight = r.weight();
      
      weightedSum = weightedSum.add(rating.multiply(weight));
      totalWeight = totalWeight.add(weight);
    }
    
    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    
    BigDecimal maxPossible = totalWeight.multiply(BigDecimal.valueOf(5));
    return weightedSum.multiply(BigDecimal.valueOf(100))
        .divide(maxPossible, 2, RoundingMode.HALF_UP);
  }
  
  /**
   * Calculates ticket-category percentage scores for tickets created between the given period.
   * Uses weighted average based on category weights, and expresses scores as percentages (0–100).
   * <p>
   * Example:
   * - Ticket 1 has one rating in category 1: rating 4, weight 2 → (4×2)/(2×5) = 40% score
   * - Ticket 1 has rating 5 in category 2, weight 1 → (5×1)/(1×5) = 100% score
   *
   * @param start start datetime (inclusive)
   * @param end   end datetime (inclusive)
   * @return map of ticketId to map of categoryId to percentage score
   */
  public Map<Integer, Map<Integer, BigDecimal>> getScoresByTicket(LocalDateTime start, LocalDateTime end) {
    // Get all ticket-category pairs to calculate
    List<RatingWithCategoryWeight> allRatings = ratingMapper.findRatingsForTicketsCreatedBetween(start, end);
    
    // Collect distinct ticketId-categoryId pairs
    var ticketCategoryPairs = allRatings.stream()
        .map(r -> new AbstractMap.SimpleEntry<>(r.ticketId(), r.categoryId()))
        .distinct()
        .toList();
    
    Map<Integer, Map<Integer, BigDecimal>> result = new HashMap<>();
    
    for (var pair : ticketCategoryPairs) {
      int ticketId = pair.getKey();
      int categoryId = pair.getValue();
      
      BigDecimal score = calculateScoreForTicketCategory(ticketId, categoryId, start, end);
      
      result.computeIfAbsent(ticketId, k -> new HashMap<>()).put(categoryId, score);
    }
    
    return result;
  }
  
  public BigDecimal getOverallScore(LocalDateTime start, LocalDateTime end) {
    // Fetch all ratings weighted by category weights between dates
    List<RatingWithCategoryWeight> ratings = ratingMapper.findRatingsCreatedBetween(start, end);
    
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalWeight = BigDecimal.ZERO;
    BigDecimal maxRating = BigDecimal.valueOf(5);
    
    for (RatingWithCategoryWeight rating : ratings) {
      BigDecimal weightedRating = rating.rating().multiply(rating.weight());
      weightedSum = weightedSum.add(weightedRating);
      totalWeight = totalWeight.add(rating.weight().multiply(maxRating));
    }
    
    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    
    // Calculate percentage score
    return weightedSum.multiply(BigDecimal.valueOf(100)).divide(totalWeight, 2, RoundingMode.HALF_UP);
  }
  
  /**
   * Calculates the overall score change between the current period and the previous period.
   * Previous period is assumed to be same length immediately before current period.
   *
   * @param currentStart start datetime of the current period (inclusive)
   * @param currentEnd   end datetime of the current period (exclusive)
   * @return a Triple of (currentScore, previousScore, change)
   */
  public PeriodScoreChange calculatePeriodOverPeriodChange(LocalDateTime currentStart, LocalDateTime currentEnd) {
    // Calculate previous period based on current period duration
    Duration periodDuration = Duration.between(currentStart, currentEnd);
    LocalDateTime previousStart = currentStart.minus(periodDuration);
    LocalDateTime previousEnd = currentStart;
    
    BigDecimal currentScore = getOverallScore(currentStart, currentEnd);
    BigDecimal previousScore = getOverallScore(previousStart, previousEnd);
    
    BigDecimal change = currentScore.subtract(previousScore).setScale(2, RoundingMode.HALF_UP);
    
    return new PeriodScoreChange(currentScore, previousScore, change);
  }
  
  // A simple DTO to hold the 3 values
  public static record PeriodScoreChange(BigDecimal currentScore, BigDecimal previousScore, BigDecimal change) {
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

