package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.store.TicketScoreCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.calculator.fn.TicketScoreCalculator;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketScoringService implements TicketScoreCalculator {
  
  private static final Logger LOG = LoggerFactory.getLogger(TicketScoringService.class);
  
  private final TicketScoreCacheStore cacheStore;
  private final RatingDao ratingDao;
  
  public TicketScoringService(TicketScoreCacheStore cacheStore, RatingDao ratingDao) {
    this.cacheStore = cacheStore;
    this.ratingDao = ratingDao;
  }
  
  /**
   * Computes and caches the weighted score for a ticket using ratings and category weights.
   *
   * @param ticketId ticket ID
   * @return percentage score between 0 and 100
   */
  public BigDecimal getTicketScore(Integer ticketId) {
    return cacheStore.getOrCalculate(ticketId, this::calculate);
  }
  
  @Override
  public BigDecimal calculate(Integer ticketId) {
    LOG.debug("Calculating score: ticketId={}", ticketId);
    
    var ratingMap = getRatingMap(ticketId);
    var weightMap = ratingDao.getCategoryWeightMap();
    var score = ScoreCalculator.calculateScore(ratingMap, weightMap)
        .setScale(6, RoundingMode.HALF_EVEN);
    
    LOG.debug("Calculated score={}, ticketId={}", score, ticketId);
    return score;
  }
  
  private Map<Integer, BigDecimal> getRatingMap(Integer ticketId) {
    LOG.debug("Fetching ratings: ticketId={}", ticketId);
    return ratingDao
        .fetchRatingsByTicketId(ticketId)
        .stream()
        .collect(Collectors.toMap(Rating::ratingCategoryId, Rating::rating));
  }
}
