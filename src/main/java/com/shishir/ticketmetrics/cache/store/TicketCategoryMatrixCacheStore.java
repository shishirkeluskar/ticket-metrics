package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.cache.fn.TicketCategoryMatrixCalculator;
import com.shishir.ticketmetrics.model.TicketXCategoryScores;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class TicketCategoryMatrixCacheStore {
  @Cacheable(value = "ticketXCategoryScoresByTicketId", key = "#ticketId")
  public TicketXCategoryScores getOrCalculate(Integer ticketId, TicketCategoryMatrixCalculator calculator) {
    return calculator.calculate(ticketId);
  }
}
