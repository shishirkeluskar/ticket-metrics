package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.calculator.fn.TicketScoreCalculator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TicketScoreCacheStore {
  
  @Cacheable(value = "ticketScoresByTicketId", key = "#ticketId")
  public BigDecimal getScoreOrCalculate(Integer ticketId, TicketScoreCalculator calculator) {
    return calculator.calculate(ticketId);
  }
}
