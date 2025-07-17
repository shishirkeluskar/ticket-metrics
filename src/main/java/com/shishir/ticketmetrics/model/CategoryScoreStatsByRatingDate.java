package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CategoryScoreStatsByRatingDate(
    Integer categoryId,
    LocalDate ratingDate,
    Long ratingCount,
    BigDecimal ratingSum,
    BigDecimal ratingAverage,
    BigDecimal categoryWeight,
    BigDecimal scoreAverage
) {
  public static CategoryScoreStatsByRatingDate of(
      Integer categoryId,
      LocalDate ratingDate,
      Long ratingCount,
      BigDecimal ratingSum,
      BigDecimal ratingAverage,
      BigDecimal categoryWeight,
      BigDecimal scoreAverage
  ) {
    return new CategoryScoreStatsByRatingDate(categoryId, ratingDate, ratingCount, ratingSum, ratingAverage, categoryWeight, scoreAverage);
  }
}
