package com.shishir.ticketmetrics.model;

import java.util.List;

public record TicketXCategoryScores(
    Integer ticketId,
    List<CategoryScoreByTicket> categoryScoreByTickets
) {
  public static TicketXCategoryScores of(Integer ticketId, List<CategoryScoreByTicket> categoryScoreByTickets) {
    return new TicketXCategoryScores(ticketId, categoryScoreByTickets);
  }
}
