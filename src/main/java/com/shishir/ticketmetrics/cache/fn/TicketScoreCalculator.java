package com.shishir.ticketmetrics.cache.fn;

import java.math.BigDecimal;

@FunctionalInterface
public interface TicketScoreCalculator {
  BigDecimal calculate(Integer ticketId);
}
