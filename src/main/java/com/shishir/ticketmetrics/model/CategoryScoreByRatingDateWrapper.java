package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.util.List;

public record CategoryScoreByRatingDateWrapper(
    Integer categoryId,
    Long ratingsCount,
    BigDecimal averageScore,
    List<CategoryScoreByRatingDate> timeline
) {
  public static CategoryScoreByRatingDateWrapper of(
      Integer categoryId,
      Long ratingsCount,
      BigDecimal averageScore,
      List<CategoryScoreByRatingDate> timeline
  ) {
    return new CategoryScoreByRatingDateWrapper(categoryId, ratingsCount, averageScore, timeline);
  }
}
