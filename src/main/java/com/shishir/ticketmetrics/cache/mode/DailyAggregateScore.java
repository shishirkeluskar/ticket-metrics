package com.shishir.ticketmetrics.cache.mode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record DailyAggregateScore(
    LocalDate date,
    BigDecimal weightedSum,
    BigDecimal totalWeight
) {
  public BigDecimal toPercentage() {
    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return weightedSum.multiply(BigDecimal.valueOf(100))
        .divide(totalWeight.multiply(BigDecimal.valueOf(5)), 2, RoundingMode.HALF_UP);
  }
}
