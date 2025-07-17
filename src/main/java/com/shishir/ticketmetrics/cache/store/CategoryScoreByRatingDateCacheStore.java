package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.cache.fn.CategoryScoreByRatingDateCalculator;
import com.shishir.ticketmetrics.model.CategoryRatingStatsByRatingDateDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CategoryScoreByRatingDateCacheStore {
  
  @Cacheable(value = "categoryScoreByRatingDate", key = "#date")
  public CategoryRatingStatsByRatingDateDto getOrCalculate(LocalDate date, CategoryScoreByRatingDateCalculator calculator) {
    return calculator.calculate(date);
  }
}
