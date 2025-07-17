package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CategoryRatingStatsByRatingDateDto(
    Integer categoryId,
    LocalDate ratingDate,
    Long ratingCount,
    BigDecimal ratingSum,
    BigDecimal ratingAverage
) {
  public CategoryRatingStatsByRatingDateDto of(
      Integer categoryId,
      LocalDate ratingDate,
      Long ratingCount,
      BigDecimal ratingSum,
      BigDecimal ratingAverage) {
    return new CategoryRatingStatsByRatingDateDto(categoryId, ratingDate, ratingCount, ratingSum, ratingAverage);
  }
}
