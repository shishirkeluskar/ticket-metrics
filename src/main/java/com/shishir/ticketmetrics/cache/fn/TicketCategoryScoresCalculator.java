package com.shishir.ticketmetrics.cache.fn;

import com.shishir.ticketmetrics.model.TicketXCategoryScores;

@FunctionalInterface
public interface TicketCategoryScoresCalculator {
  TicketXCategoryScores calculate(Integer ticketId);
}
