package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.model.RatingWithCategoryWeight;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketCategoryScoresService {
  private final RatingDao ratingDao;
  
  public TicketCategoryScoresService(RatingDao ratingDao) {
    this.ratingDao = ratingDao;
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
  public Map<Integer, Map<Integer, BigDecimal>> getScoresByTicket(LocalDate start, LocalDate end) {
    // Get all ticket-category pairs to calculate
    List<RatingWithCategoryWeight> allRatings = ratingDao.findRatingsForTicketsCreatedBetween(start, end);
    
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
  
  @Cacheable(value = "ticketCategoryScores", key = "#ticketId + '-' + #categoryId + '-' + #start.toString() + '-' + #end.toString()")
  public BigDecimal calculateScoreForTicketCategory(int ticketId, int categoryId, LocalDate start, LocalDate end) {
    // Call mapper to get ratings for this ticket-category in period
    List<RatingWithCategoryWeight> ratings = ratingDao.findRatingsForTicketCategoryBetween(ticketId, categoryId, start, end);
    
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
        .divide(maxPossible, 6, RoundingMode.HALF_UP);
  }
}
