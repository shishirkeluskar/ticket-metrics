package com.shishir.ticketmetrics.cache.fn;

import com.shishir.ticketmetrics.model.TicketXCategoryScores;

@FunctionalInterface
public interface TicketCategoryMatrixCalculator {
  TicketXCategoryScores calculate(Integer ticketId);
}
