package com.shishir.ticketmetrics.calculator.fn;

import java.math.BigDecimal;

@FunctionalInterface
public interface TicketScoreCalculator {
  BigDecimal calculate(Integer ticketId);
}
