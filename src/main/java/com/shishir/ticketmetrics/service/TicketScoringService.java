package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.calculator.TicketScoreCalculator;
import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.Rating;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketScoringService {
  
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
    List<Rating> ratings = ratingMapper.findRatingsByTicketId(ticketId);
    var ratingMap = ratings.stream().collect(Collectors.toMap(Rating::ratingCategoryId, Rating::rating));
    var weightMap = ratingCategoryService.getCategoryWeightMap();
    
    return TicketScoreCalculator.calculateScore(ratingMap, weightMap);
  }
}
