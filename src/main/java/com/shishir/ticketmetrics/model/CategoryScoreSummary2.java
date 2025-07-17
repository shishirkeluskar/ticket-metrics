package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CategoryScoreSummary2(
    Integer categoryId,
    Long ratingsCount,
    BigDecimal averageScore,
    List<Timeline> timeline
) {
  public static CategoryScoreSummary2 of(
      Integer categoryId,
      Long ratingsCount,
      BigDecimal averageScore,
      List<Timeline> timeline
  ) {
    return new CategoryScoreSummary2(categoryId, ratingsCount, averageScore, timeline);
  }
  
  public record Timeline(
      LocalDate date,
      BigDecimal score
  ) {
    public static Timeline of(LocalDate date, BigDecimal score) {
      return new Timeline(date, score);
    }
  }
}
