package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.calculator.fn.OverallDailyScoreCalculator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class OverallDailyScoreCacheStore {
  
  @Cacheable(value = "overallDailyScore", key = "#date")
  public BigDecimal getScoreOrCalculate(LocalDate date, OverallDailyScoreCalculator calculator) {
    return calculator.calculate(date);
  }
}
