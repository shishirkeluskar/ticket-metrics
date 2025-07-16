package com.shishir.ticketmetrics.model;

import java.util.List;

public record TicketXCategoryScores(
    Integer ticketId,
    List<CategoryScore> categoryScores
) {
  public static TicketXCategoryScores of(Integer ticketId, List<CategoryScore> categoryScores) {
    return new TicketXCategoryScores(ticketId, categoryScores);
  }
}
