package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.calculator.fn.OverallDailyScoreCalculator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class OverallDailyScoreCacheStore {
  
  @Cacheable(value = "overallDailyScoreByDate", key = "#date")
  public BigDecimal getOrCalculate(LocalDate date, OverallDailyScoreCalculator calculator) {
    return calculator.calculate(date);
  }
}
