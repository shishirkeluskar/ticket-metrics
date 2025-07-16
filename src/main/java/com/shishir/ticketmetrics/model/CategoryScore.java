package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;

public record CategoryScore(
    Integer categoryId,
    BigDecimal score
) {
  public static CategoryScore of(Integer categoryId, BigDecimal score) {
    return new CategoryScore(categoryId, score);
  }
}
