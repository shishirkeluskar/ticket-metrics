package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.calculator.TicketScoreCalculator;
import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketScoringService {
  
  private static final Logger LOG = LoggerFactory.getLogger(TicketScoringService.class);
  private final RatingMapper ratingMapper;
  private final RatingCategoryService ratingCategoryService;
  
  public TicketScoringService(RatingMapper ratingMapper, RatingCategoryService ratingCategoryService) {
    this.ratingMapper = ratingMapper;
    this.ratingCategoryService = ratingCategoryService;
  }
  
  /**
   * Computes and caches the weighted score for a ticket using ratings and category weights.
   *
   * @param ticketId ticket ID
   * @return percentage score between 0 and 100
   */
  @Cacheable(value = "ticketScoresByTicketId", key = "#ticketId")
  public double computeScore(Integer ticketId) {
    LOG.debug("Calculating score for ticketId={}", ticketId);
    
    List<Rating> ratings = ratingMapper.findRatingsByTicketId(ticketId);
    var ratingMap = ratings.stream().collect(Collectors.toMap(Rating::ratingCategoryId, Rating::rating));
    var weightMap = ratingCategoryService.getCategoryWeightMap();
    var score = TicketScoreCalculator.calculateScore(ratingMap, weightMap);
    
    LOG.debug("Calculated score={} for ticketId={}", score, ticketId);
    return score;
  }
}
