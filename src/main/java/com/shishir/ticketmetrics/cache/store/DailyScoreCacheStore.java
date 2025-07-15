package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DailyScoreCacheStore {
  private static final Logger LOG = LoggerFactory.getLogger(DailyScoreCacheStore.class);
  private final RatingDao ratingDao;
  
  public DailyScoreCacheStore(RatingDao ratingDao) {
    this.ratingDao = ratingDao;
  }
  
  @Cacheable(value = "dailyScores", key = "#date")
  public BigDecimal getScoreForDate(LocalDate date) {
    return getScoreForDate2(date);
    
//    LOG.debug("Fetching Rating+Weights for date={}", date);
//    var ratings = ratingDao.fetchRatingsByRatingDate(date);
//    var ratingMap = getRatingMap(ratings);
//    var weightMap = ratingDao.getCategoryWeightMap();
//    var score = ScoreCalculator.calculateScore(ratingMap, weightMap);
//    return score;
  }
  
  private Map<Integer, BigDecimal> getRatingMap(List<Rating> ratings) {
    return ratings.stream()
        .collect(Collectors.groupingBy(
            Rating::ratingCategoryId,
            Collectors.reducing(
                BigDecimal.ZERO,
                Rating::rating,
                BigDecimal::add
            )
        ));
  }
  
  @Cacheable(value = "dailyScores2", key = "#date")
  public BigDecimal getScoreForDate2(LocalDate date) {
    LOG.debug("Fetching Rating+Weights for date={}", date);
    var ratings = ratingDao.fetchRatingsByRatingDate(date);
    var weightMap = ratingDao.getCategoryWeightMap();
    
    var scores = ratings.stream()
        .map(r -> ScoreCalculator.calculateScore(Map.of(r.ratingCategoryId(), r.rating()), weightMap))
        .collect(Collectors.toList());
    
    var score = scores.stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    int count = scores.size();
    var sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    var average = count > 0 ? sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
    
    return average;
  }
}
