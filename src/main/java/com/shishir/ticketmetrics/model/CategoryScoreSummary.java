package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryScoreSummary {
  private int totalRatings;
  private BigDecimal totalWeightedScore = BigDecimal.ZERO;
  private final Map<LocalDateTime, BigDecimal> dateScores = new LinkedHashMap<>();
  
  public void addScore(LocalDateTime date, BigDecimal score, int count) {
    dateScores.put(date, score);
    totalRatings += count;
    totalWeightedScore = totalWeightedScore.add(score.multiply(BigDecimal.valueOf(count)));
  }
  
  public int getTotalRatings() {
    return totalRatings;
  }
  
  public BigDecimal getFinalAverageScore() {
    if (totalRatings == 0) return BigDecimal.ZERO;
    return totalWeightedScore.divide(BigDecimal.valueOf(totalRatings), 2, BigDecimal.ROUND_HALF_UP);
  }
  
  public Map<LocalDateTime, BigDecimal> getDateScores() {
    return dateScores;
  }
}
