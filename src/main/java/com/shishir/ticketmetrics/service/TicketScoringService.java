package com.shishir.ticketmetrics.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TicketScoringService {
  
  /**
   * Computes and caches the weighted score for the given ticket.
   * Uses ticket ID as the cache key.
   *
   * @param ticketId         unique ID of the ticket
   * @param categoryRatings  map of category ID → rating (scale 0–5)
   * @param categoryWeights  map of category ID → weight (importance multiplier)
   * @return ticket score as a percentage (0.00–100.00)
   */
  @Cacheable(value = "ticketScores", key = "#ticketId")
  public double computeScore(
      int ticketId,
      Map<Integer, BigDecimal> categoryRatings,
      Map<Integer, BigDecimal> categoryWeights
  ) {
    return TicketScoreCalculator.calculateScore(categoryRatings, categoryWeights);
  }
}
