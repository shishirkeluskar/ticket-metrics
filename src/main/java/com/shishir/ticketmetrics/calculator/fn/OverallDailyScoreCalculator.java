package com.shishir.ticketmetrics.calculator.fn;

import java.math.BigDecimal;
import java.time.LocalDate;

@FunctionalInterface
public interface OverallDailyScoreCalculator {
  BigDecimal calculate(LocalDate startDate);
}
