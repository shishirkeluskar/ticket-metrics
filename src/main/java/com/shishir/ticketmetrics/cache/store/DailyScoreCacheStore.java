package com.shishir.ticketmetrics.cache.store;

import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.model.Rating;
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
  private final RatingDao ratingDao;
  
  public DailyScoreCacheStore(RatingDao ratingDao) {
    this.ratingDao = ratingDao;
  }
  
  @Cacheable(value = "dailyScores", key = "#date")
  public BigDecimal getScoreForDate(LocalDate date) {
    LOG.debug("Fetching Rating+Weights for date={}", date);
    var ratings = ratingDao.fetchRatingsByRatingDate(date);
    var ratingMap = getRatingMap(ratings);
    var weightMap = ratingDao.getCategoryWeightMap();
    var score = ScoreCalculator.calculateScore(ratingMap, weightMap);
    return score;
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
}
