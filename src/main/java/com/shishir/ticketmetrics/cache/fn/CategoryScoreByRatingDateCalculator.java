package com.shishir.ticketmetrics.cache.fn;

import com.shishir.ticketmetrics.model.CategoryScoreByRatingDate;

import java.time.LocalDate;

@FunctionalInterface
public interface CategoryScoreByRatingDateCalculator {
  CategoryScoreByRatingDate calculate(LocalDate date);
}
