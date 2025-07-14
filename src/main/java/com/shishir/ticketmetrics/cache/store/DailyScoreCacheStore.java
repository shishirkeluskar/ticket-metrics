package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.cache.mode.DailyAggregateScore;
import com.shishir.ticketmetrics.calculator.TicketScoreCalculator;
import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.RatingWithCategoryWeight2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DailyScoreCacheStore {
  private static final Logger LOG = LoggerFactory.getLogger(DailyScoreCacheStore.class);
  private final RatingMapper ratingMapper;
  
  public DailyScoreCacheStore(RatingMapper ratingMapper) {
    this.ratingMapper = ratingMapper;
  }
  
  @Cacheable(value = "dailyScores", key = "#date")
  public DailyAggregateScore getScoreForDate(LocalDate date) {
    var ratings = getRatingsByDate(date);
    
    var weightedSum = ratings.stream()
        .map(r -> r.rating().multiply(r.weight()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    var totalWeight = ratings.stream()
        .map(RatingWithCategoryWeight2::weight)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    LOG.debug("date={}, weightedSum={}, totalWeight={},", date, weightedSum, totalWeight);
    return new DailyAggregateScore(date, weightedSum, totalWeight);
  }
  
  private List<RatingWithCategoryWeight2> getRatingsByDate(LocalDate date) {
    LOG.debug("Fetching Rating+Weights for date={}", date);
    return ratingMapper.findRatingsByDate(date);
  }
  
  public BigDecimal getScoreForDate2(LocalDate date) {
    var ratings = getRatingsByDate(date);
    
    var summedRatingsByCategory  = getSummedRatingsByCategory(ratings);
    var weightMap = ratingMapper.getCategoryWeightMap();
    var score = TicketScoreCalculator.calculateScore(summedRatingsByCategory, weightMap);
    
    return score;
  }
  
  private Map<Integer, BigDecimal> getSummedRatingsByCategory(List<RatingWithCategoryWeight2> ratings) {
    return ratings.stream()
        .collect(Collectors.groupingBy(
            RatingWithCategoryWeight2::categoryId,
            Collectors.reducing(
                BigDecimal.ZERO,
                RatingWithCategoryWeight2::rating,
                BigDecimal::add
            )
        ));
  }
}
