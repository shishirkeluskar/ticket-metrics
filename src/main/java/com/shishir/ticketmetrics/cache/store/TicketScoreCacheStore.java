package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.cache.fn.TicketScoreCalculator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TicketScoreCacheStore {
  
  @Cacheable(value = "ticketScoreByTicketId", key = "#ticketId")
  public BigDecimal getOrCalculate(Integer ticketId, TicketScoreCalculator calculator) {
    return calculator.calculate(ticketId);
  }
}
