package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.calculator.TicketScoreCalculator;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketScoringService {
  
  private static final Logger LOG = LoggerFactory.getLogger(TicketScoringService.class);
  private final RatingDao ratingDao;
  
  public TicketScoringService(RatingDao ratingDao) {
    this.ratingDao = ratingDao;
  }
  
  /**
   * Computes and caches the weighted score for a ticket using ratings and category weights.
   *
   * @param ticketId ticket ID
   * @return percentage score between 0 and 100
   */
  @Cacheable(value = "ticketScores", key = "#ticketId")
  public double getTicketScore(Integer ticketId) {
    LOG.debug("Calculating score: ticketId={}", ticketId);
    
    var ratingMap = getRatingMap(ticketId);
    var weightMap = ratingDao.getCategoryWeightMap();
    var score = TicketScoreCalculator.calculateScore(ratingMap, weightMap)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
    
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
