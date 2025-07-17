package com.shishir.ticketmetrics.cache.fn;

import com.shishir.ticketmetrics.model.CategoryRatingStatsByRatingDateDto;

import java.time.LocalDate;

@FunctionalInterface
public interface CategoryScoreByRatingDateCalculator {
  CategoryRatingStatsByRatingDateDto calculate(LocalDate date);
}
