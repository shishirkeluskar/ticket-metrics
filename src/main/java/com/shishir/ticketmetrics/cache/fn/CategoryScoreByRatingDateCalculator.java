package com.shishir.ticketmetrics.cache.fn;

import com.shishir.ticketmetrics.model.CategoryScoreStatsByRatingDate;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface CategoryScoreByRatingDateCalculator {
  List<CategoryScoreStatsByRatingDate> calculate(LocalDate date);
}
