package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.fn.CategoryScoreByRatingDateCalculator;
import com.shishir.ticketmetrics.cache.store.CategoryScoreByRatingDateCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.model.CategoryScoreStatsByRatingDate;
import com.shishir.ticketmetrics.model.CategoryScoreSummary2;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.dao.RatingStatsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetCategoryTimelineScoreService2 implements CategoryScoreByRatingDateCalculator {
  public static final Logger LOG = LoggerFactory.getLogger(GetCategoryTimelineScoreService2.class);
  
  private final CategoryScoreByRatingDateCacheStore cacheStore;
  private final RatingStatsDao ratingStatsDao;
  private final RatingDao ratingDao;
  
  public GetCategoryTimelineScoreService2(CategoryScoreByRatingDateCacheStore cacheStore, RatingStatsDao ratingStatsDao, RatingDao ratingDao) {
    this.cacheStore = cacheStore;
    this.ratingStatsDao = ratingStatsDao;
    this.ratingDao = ratingDao;
  }
  
  public List<CategoryScoreSummary2> getCategoryTimelineScores(LocalDate startDate, LocalDate endDate) {
    var x = getScoresInRange(startDate, endDate);
    
    LOG.debug("abc={}", x);
    return x;
  }
  
  private List<CategoryScoreSummary2> getScoresInRange(LocalDate startDate, LocalDate endDate) {
    var categoryScoreStatsMap = startDate.datesUntil(endDate.plusDays(1))
        .map(date -> cacheStore.getOrCalculate(date, this::calculate))
        .flatMap(Collection::stream)
        .collect(Collectors.groupingBy(CategoryScoreStatsByRatingDate::categoryId, Collectors.toList()));
    
    var categoryScoreSummaries = categoryScoreStatsMap.entrySet().stream()
        .map(e -> {
          var categoryId = e.getKey();
          var ratingsCount = 0L;
          var scoreSum = BigDecimal.ZERO;
          var count = 0L;
          var timeline = new ArrayList<CategoryScoreSummary2.Timeline>();
          
          // Step 4: Compute average of scores per category
          for (var categoryScoreStats : e.getValue()) {
            count += 1;
            ratingsCount += categoryScoreStats.ratingCount();
            scoreSum = scoreSum.add(categoryScoreStats.scoreAverage());
            timeline.add(CategoryScoreSummary2.Timeline.of(categoryScoreStats.ratingDate(), categoryScoreStats.scoreAverage()));
          }
          var scoreAverage = count > 0 ? scoreSum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
          
          var out = CategoryScoreSummary2.of(
              categoryId,
              ratingsCount,
              scoreAverage,
              timeline
          );
          
          return out;
        })
        .toList();
    
    return categoryScoreSummaries;
  }
  
  @Override
  public List<CategoryScoreStatsByRatingDate> calculate(LocalDate date) {
    LOG.debug("Starting overall score calculation for date={}", date);
    
    // Step 1: Fetch aggregated rating stats by category for the given date
    var categoryStats = ratingStatsDao.fetchCategoryStatsByRatingDate(date);
    if (categoryStats.isEmpty()) {
      LOG.info("No rating data found for date={}", date);
      return null;
    }
    LOG.info("Fetched {} category rating stats for date={}", categoryStats.size(), date);
    
    // Step 2: Load category weights
    var weightMap = ratingDao.getCategoryWeightMap();
    LOG.debug("Loaded {} category weights", weightMap);
    
    // Step 3: Calculate individual scores per category
    var categoryScores = categoryStats.stream()
        .map(stats -> {
              var categoryId = stats.categoryId();
              var ratingAverage = stats.ratingAverage();
              var weight = weightMap.get(categoryId);
              var score = ScoreCalculator.calculateScore(Map.of(categoryId, ratingAverage), weightMap);
              var categoryScore = CategoryScoreStatsByRatingDate.of(
                  categoryId,
                  date,
                  stats.ratingCount(),
                  stats.ratingSum(),
                  stats.ratingAverage(),
                  weight,
                  score
              );
              
              LOG.debug("date={}, categoryScore={}", date, categoryScore);
              return categoryScore;
            }
        )
        .toList();
    LOG.debug("date={}, categoryScore={}", date, categoryScores);
    return categoryScores;
  }
}
