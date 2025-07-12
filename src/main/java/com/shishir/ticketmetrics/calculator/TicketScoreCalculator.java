package com.shishir.ticketmetrics.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class TicketScoreCalculator {
  private static final Logger LOG = LoggerFactory.getLogger(TicketScoreCalculator.class);
  private static final BigDecimal MIN_SCALE = new BigDecimal("0.0");
  private static final BigDecimal MAX_SCALE = new BigDecimal("5.0");
  private static final BigDecimal HUNDRED = new BigDecimal("100.0");
  
  /**
   * Calculate the weighted score of a ticket based on individual
   * category ratings, and their associated weights.
   *
   * <p>
   * Step 1: Each rating is normalized to percentage (0 - 100) by converting
   * them from the original (0 - 5) scale:
   * <pre>
   *   normalizedScore = (rating / 5.0) * 100
   * </pre>
   * Step 2: Then each normalized score is multiplied by its category's weight:
   * <pre>
   *   weightedScore = normalizedScore * weight
   * </pre>
   * Step 3: The final ticket score is calculated as the weighted average:
   * <pre>
   *   finalScore = sum(weightedScores) / sum(weights)
   * </pre>
   * </p>
   * <p>
   * If no valid ratings with matching weights are found, the score defaults to 0.0.
   *
   * <p><b>Example 1:</b></p>
   * <pre>
   *     categoryRatings = { 1 → 5, 2 → 3 }
   *     categoryWeights = { 1 → 2, 2 → 1 }
   *
   *     normalized scores:
   *       1 → (5 / 5.0) * 100 = 100
   *       2 → (3 / 5.0) * 100 = 60
   *
   *     weighted scores:
   *       1 → 100 * 2 = 200
   *       2 → 60 * 1 = 60
   *
   *     final score = (200 + 60) / (2 + 1) = 260 / 3 = 86.67%
   * </pre>
   *
   * <p><b>Example 2:</b></p>
   * <pre>
   *     categoryRatings = { 1 → 2 }
   *     categoryWeights = { 1 → 3 }
   *
   *     normalized score: (2 / 5.0) * 100 = 40
   *     weighted score: 40 * 3 = 120
   *     final score = 120 / 3 = 40.0%
   * </pre>
   *
   * @param categoryRatings Map of categoryId → rating (0–5)
   * @param categoryWeights Map of categoryId → weight (integer > 0)
   * @return score as percentage (0–100), rounded to 2 decimals
   */
  public static double calculateScore(
      Map<Integer, BigDecimal> categoryRatings,
      Map<Integer, BigDecimal> categoryWeights
  ) {
    var weightedScoreSum = BigDecimal.ZERO;
    var totalWeight = BigDecimal.ZERO;
    
    for (Map.Entry<Integer, BigDecimal> ratingEntry : categoryRatings.entrySet()) {
      int categoryId = ratingEntry.getKey();
      var rating = ratingEntry.getValue();
      
      validate(categoryId, rating, categoryWeights);
      
      if (!categoryWeights.containsKey(categoryId)) continue;
      
      var weight = categoryWeights.get(categoryId);
      // Step 1: Step 1: Each rating is normalized to percentage (0 - 100) by converting
      // them from the original (0 - 5) scale.
      var normalizedScore = rating
          .divide(MAX_SCALE, 6, RoundingMode.HALF_UP)
          .multiply(HUNDRED);
      
      // Step 2: Then each normalized score is multiplied by its category's weight.
      var weightedScore = normalizedScore.multiply(weight);
      weightedScoreSum = weightedScore.add(weightedScoreSum);
      
      // Step2.1: Calculate total weight.
      totalWeight = totalWeight.add(weight);
    }
    
    // Step 3: The final ticket score is calculated as the weighted average.
    var score = 0.0;
    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
      score = 0.0;
    } else {
      score = weightedScoreSum
          .divide(totalWeight, 2, RoundingMode.HALF_UP)
          .doubleValue();
    }
    
    LOG.debug("Calculated score={} for rating={} and weights={}", score, categoryRatings, categoryWeights);
    return score;
  }
  
  private static void validate(Integer categoryId, BigDecimal rating, Map<Integer, BigDecimal> categoryWeights) {
    requireRatingNotNull(categoryId, rating);
    requireRatingWithinBounds(categoryId, rating);
    // requireWeightForCategory(categoryId, categoryWeights);
  }
  
  private static void requireRatingNotNull(Integer categoryId, BigDecimal rating) {
    if (rating == null) {
      throw new IllegalArgumentException(
          String.format("Rating is null for categoryId=%d".formatted(categoryId))
      );
    }
  }
  
  private static void requireRatingWithinBounds(Integer categoryId, BigDecimal rating) {
    if (rating.compareTo(MIN_SCALE) < 0 || rating.compareTo(MAX_SCALE) > 0) {
      throw new IllegalArgumentException(
          String.format("Rating=%d is out bounds for categoryId=%s. Bounds %s to %s."
              .formatted(rating, categoryId, MIN_SCALE, MAX_SCALE))
      );
    }
  }
  
  private static void requireWeightForCategory(Integer categoryId, Map<Integer, BigDecimal> categoryWeights) {
    if (!categoryWeights.containsKey(categoryId)) {
      throw new IllegalArgumentException(
          String.format("Missing weight for categoryId=%d".formatted(categoryId))
      );
    }
  }
}
