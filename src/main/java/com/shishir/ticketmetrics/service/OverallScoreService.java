package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.store.OverallDailyScoreCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.calculator.fn.OverallDailyScoreCalculator;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.dao.RatingStatsDao;
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
  private final OverallDailyScoreCacheStore cacheStore;
  private final RatingDao ratingDao;
  private final RatingStatsDao ratingStatsDao;
  
  public OverallScoreService(OverallDailyScoreCacheStore cacheStore, RatingDao ratingDao, RatingStatsDao ratingStatsDao) {
    this.cacheStore = cacheStore;
    this.ratingDao = ratingDao;
    this.ratingStatsDao = ratingStatsDao;
  }
  
  public BigDecimal getOverallScore(LocalDate startDate, LocalDate endDate) {
    LOG.debug("Calculating overall score: startDate={}, endDate={}", startDate, endDate);
    var scores = getScoresInRange(startDate, endDate);
    var count = scores.size();
    var sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    var avg = count > 0 ? sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
    LOG.debug("Calculated overall score: sum={}, count={}, avg={}, startDate={}, endDate={}", sum, count, avg, startDate, endDate);
    return avg;
  }
  
  private List<BigDecimal> getScoresInRange(LocalDate startDate, LocalDate endDate) {
    return startDate.datesUntil(endDate.plusDays(1))
        .map(date -> cacheStore.getOrCalculate(date, this::calculate))
        .toList();
  }
  
  @Override
  public BigDecimal calculate(LocalDate date) {
    LOG.debug("Starting overall score calculation for date={}", date);
    
    // Step 1: Fetch aggregated rating stats by category for the given date
    var categoryStats = ratingStatsDao.fetchCategoryStatsByRatingDate(date);
    if (categoryStats.isEmpty()) {
      LOG.info("No rating data found for date={}", date);
      return BigDecimal.ZERO;
    }
    LOG.info("Fetched {} category rating stats for date={}", categoryStats.size(), date);
    
    // Step 2: Load category weights
    var weightMap = ratingDao.getCategoryWeightMap();
    LOG.debug("Loaded {} category weights", weightMap);
    
    // Step 3: Calculate individual scores per category
    var scores = categoryStats.stream()
        .map(stats -> {
              var categoryId = stats.categoryId();
              var ratingAverage = stats.ratingAverage();
              var weight = weightMap.get(categoryId);
              
              var score = ScoreCalculator.calculateScore(Map.of(categoryId, ratingAverage), weightMap);
              
              LOG.debug("categoryId={}, ratingAverage={}, weight={}, score={}", categoryId, ratingAverage, weight, score);
              return score;
            }
        )
        .collect(Collectors.toSet());
    
    // Step 4: Compute average of scores
    int count = scores.size();
    var sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    var avg = count > 0 ? sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
    
    LOG.debug("Calculated overall score for date={}, avg={}, sum={}, count={}", date, avg, sum, count);
    return avg;
  }
}
