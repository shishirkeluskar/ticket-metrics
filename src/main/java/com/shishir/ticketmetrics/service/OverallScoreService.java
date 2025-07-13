package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.mode.DailyAggregateScore;
import com.shishir.ticketmetrics.cache.store.DailyScoreCacheStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class OverallScoreService {
  private static final Logger LOG = LoggerFactory.getLogger(OverallScoreService.class);
  private final DailyScoreCacheStore cacheStore;
  
  public OverallScoreService(DailyScoreCacheStore cacheStore) {
    this.cacheStore = cacheStore;
  }
  
  public BigDecimal getOverallScore(LocalDate startDate, LocalDate endDate) {
    LOG.debug("Calculating overall score: startDate={}, endDate={}", startDate, endDate);
    return calculateAverage(getScoresInRange(startDate, endDate));
  }
  
  private static BigDecimal calculateAverage(List<DailyAggregateScore> dailyAggregateScores) {
    int count = dailyAggregateScores.size();
    var sum = dailyAggregateScores.stream()
        .map(DailyAggregateScore::toPercentage)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    var average = count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    LOG.debug("Calculated overall score: sum={}, count={}, average={}", sum, count, average);
    return average;
  }
  
  private List<DailyAggregateScore> getScoresInRange(LocalDate startDate, LocalDate endDate) {
    return startDate.datesUntil(endDate.plusDays(1))
        .map(cacheStore::getScoreForDate)
        .toList();
  }
}
