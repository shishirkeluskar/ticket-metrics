package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.store.OverallDailyScoreCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.calculator.fn.OverallDailyScoreCalculator;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OverallScoreService implements OverallDailyScoreCalculator {
  private static final Logger LOG = LoggerFactory.getLogger(OverallScoreService.class);
  private final OverallDailyScoreCacheStore overallDailyScoreCacheStore;
  private final RatingDao ratingDao;
  
  public OverallScoreService(OverallDailyScoreCacheStore overallDailyScoreCacheStore, RatingDao ratingDao) {
    this.overallDailyScoreCacheStore = overallDailyScoreCacheStore;
    this.ratingDao = ratingDao;
  }
  
  public BigDecimal getOverallScore(LocalDate startDate, LocalDate endDate) {
    LOG.debug("Calculating overall score: startDate={}, endDate={}", startDate, endDate);
    var scores = getScoresInRange(startDate, endDate);
    int count = scores.size();
    var sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    var average = count > 0 ? sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
    LOG.debug("Calculated overall score: sum={}, count={}, average={}", sum, count, average);
    return average;
  }
  
  private List<BigDecimal> getScoresInRange(LocalDate startDate, LocalDate endDate) {
    return startDate.datesUntil(endDate.plusDays(1))
        .map(it -> overallDailyScoreCacheStore.getScoreOrCalculate(it, this::calculate))
        .toList();
  }
  
  @Override
  public BigDecimal calculate(LocalDate date) {
    LOG.debug("Fetching Rating+Weights for date={}", date);
    var ratings = ratingDao.fetchRatingsByRatingDate(date);
    var weightMap = ratingDao.getCategoryWeightMap();
    
    var scores = ratings.stream()
        .map(r -> ScoreCalculator.calculateScore(Map.of(r.ratingCategoryId(), r.rating()), weightMap))
        .collect(Collectors.toList());
    
    int count = scores.size();
    var sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    var average = count > 0 ? sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
    
    return average;
  }
}
