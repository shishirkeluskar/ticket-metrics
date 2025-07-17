package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.util.List;

public record CategoryScoreSummary2(
    Integer categoryId,
    Long ratingsCount,
    BigDecimal averageScore,
    List<CategoryScoreStatsByRatingDate> timeline
) {
  public static CategoryScoreSummary2 of(
      Integer categoryId,
      Long ratingsCount,
      BigDecimal averageScore,
      List<CategoryScoreStatsByRatingDate> timeline
  ) {
    return new CategoryScoreSummary2(categoryId, ratingsCount, averageScore, timeline);
  }
}
