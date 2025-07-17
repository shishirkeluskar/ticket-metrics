package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;

public record CategoryScoreByTicket(
    Integer categoryId,
    BigDecimal score
) {
  public static CategoryScoreByTicket of(Integer categoryId, BigDecimal score) {
    return new CategoryScoreByTicket(categoryId, score);
  }
}
