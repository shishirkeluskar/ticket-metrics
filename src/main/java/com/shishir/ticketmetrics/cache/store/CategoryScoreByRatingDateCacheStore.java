package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.cache.fn.CategoryScoreByRatingDateCalculator;
import com.shishir.ticketmetrics.model.CategoryScoreStatsByRatingDate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CategoryScoreByRatingDateCacheStore {
  
  @Cacheable(value = "categoryScoreByRatingDate", key = "#date")
  public List<CategoryScoreStatsByRatingDate> getOrCalculate(LocalDate date, CategoryScoreByRatingDateCalculator calculator) {
    return calculator.calculate(date);
  }
}
