package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.fn.TicketCategoryMatrixCalculator;
import com.shishir.ticketmetrics.cache.store.TicketCategoryMatrixCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.model.CategoryScoreByTicket;
import com.shishir.ticketmetrics.model.TicketXCategoryScores;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  
  public List<TicketXCategoryScores> getTicketCategoryScores(LocalDate start, LocalDate end) {
    var tickets = ratingDao.fetchRatedTickets(start, end);
    LOG.debug("Found {} rated tickets between {} and {}", tickets.size(), start, end);
    var ticketXCategoryScores = tickets.stream()
        .map(ticketId -> cacheStore.getOrCalculate(ticketId, this::calculate))
        .filter(Objects::nonNull)
        .toList();
    LOG.debug("Calculated {} score of tickets between {} and {}", ticketXCategoryScores.size(), start, end);
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
