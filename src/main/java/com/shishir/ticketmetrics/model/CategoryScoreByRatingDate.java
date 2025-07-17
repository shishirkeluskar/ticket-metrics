package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CategoryScoreByRatingDate(
    Integer categoryId,
    BigDecimal score,
    LocalDate date,
    Long ratingsCount
) {
  public CategoryScoreByRatingDate of(
      Integer categoryId, BigDecimal score, LocalDate date, Long ratingsCount) {
    return new CategoryScoreByRatingDate(categoryId, score, date, ratingsCount);
  }
}
