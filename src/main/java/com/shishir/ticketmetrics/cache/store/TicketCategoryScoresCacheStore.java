package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.cache.fn.TicketCategoryScoresCalculator;
import com.shishir.ticketmetrics.model.TicketXCategoryScores;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class TicketCategoryScoresCacheStore {
  @Cacheable(value = "ticketXCategoryScoresByTicketId", key = "#ticketId")
  public TicketXCategoryScores getOrCalculate(Integer ticketId, TicketCategoryScoresCalculator calculator) {
    return calculator.calculate(ticketId);
  }
}
