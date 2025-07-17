package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.fn.TicketCategoryMatrixCalculator;
import com.shishir.ticketmetrics.cache.store.TicketCategoryMatrixCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.model.CategoryScoreByTicket;
import com.shishir.ticketmetrics.model.RatingWithCategoryWeight;
import com.shishir.ticketmetrics.model.TicketXCategoryScores;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketCategoryMatrixService implements TicketCategoryMatrixCalculator {
  private static final Logger LOG = LoggerFactory.getLogger(TicketCategoryMatrixService.class);
  private final RatingDao ratingDao;
  private final TicketCategoryMatrixCacheStore cacheStore;
  
  public TicketCategoryMatrixService(RatingDao ratingDao, TicketCategoryMatrixCacheStore cacheStore) {
    this.ratingDao = ratingDao;
    this.cacheStore = cacheStore;
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
  
  public List<TicketXCategoryScores> getTicketCategoryScores(LocalDate start, LocalDate end) {
    var tickets = ratingDao.fetchTickets(start, end);
    LOG.debug("Found {} tickets between {} and {}", tickets.size(), start, end);
    var ticketXCategoryScores = tickets.stream()
        .map(ticketId -> cacheStore.getOrCalculate(ticketId, this::calculate))
        .filter(Objects::nonNull)
        .toList();
    LOG.debug("Calculated {} ratings of tickets between {} and {}", ticketXCategoryScores.size(), start, end);
    return ticketXCategoryScores;
  }
  
  @Override
  public TicketXCategoryScores calculate(Integer ticketId) {
    LOG.debug("Starting ticket x category score calculation for ticketId={}", ticketId);
    var ratingMap = getRatingMap(ticketId);
    var weightMap = ratingDao.getCategoryWeightMap();
    
    if (ratingMap.isEmpty()) {
      LOG.debug("No ratings found for ticketId={}", ticketId);
      return null;
    } else {
      var ticketXCategoryScores = TicketXCategoryScores.of(
          ticketId,
          weightMap                             // iterate over each category
              .keySet()
              .stream()
              .filter(ratingMap::containsKey)   // check if ticket has rating for that category
              .map(categoryId -> {      // calculate score
                var score = ScoreCalculator.calculateScore(
                    Map.of(categoryId, ratingMap.get(categoryId)),
                    weightMap
                ).setScale(6, RoundingMode.HALF_EVEN);
                return CategoryScoreByTicket.of(categoryId, score);
              })
              .toList()
      );
      LOG.debug("Calculated ticket x category ticketId={} score={}", ticketId, ticketXCategoryScores);
      return ticketXCategoryScores;
    }
  }
  
  private Map<Integer, BigDecimal> getRatingMap(Integer ticketId) {
    LOG.debug("Fetching ratings: ticketId={}", ticketId);
    return ratingDao
        .fetchRatingsByTicketId(ticketId)
        .stream()
        .collect(Collectors.toMap(Rating::ratingCategoryId, Rating::rating));
  }
}
