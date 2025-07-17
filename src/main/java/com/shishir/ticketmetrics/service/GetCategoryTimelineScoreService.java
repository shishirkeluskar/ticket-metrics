package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.fn.CategoryScoreByRatingDateCalculator;
import com.shishir.ticketmetrics.cache.store.CategoryScoreByRatingDateCacheStore;
import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.model.CategoryScoreStatsByRatingDate;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import com.shishir.ticketmetrics.persistence.dao.RatingStatsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GetCategoryTimelineScoreService implements CategoryScoreByRatingDateCalculator {
  public static final Logger LOG = LoggerFactory.getLogger(GetCategoryTimelineScoreService.class);
  
  private final CategoryScoreByRatingDateCacheStore cacheStore;
  private final RatingStatsDao ratingStatsDao;
  private final RatingDao ratingDao;
  
  public GetCategoryTimelineScoreService(CategoryScoreByRatingDateCacheStore cacheStore, RatingStatsDao ratingStatsDao, RatingDao ratingDao) {
    this.cacheStore = cacheStore;
    this.ratingStatsDao = ratingStatsDao;
    this.ratingDao = ratingDao;
  }
  
  public List<CategoryScoreSummary> getCategoryTimelineScores(LocalDate startDate, LocalDate endDate) {
    var categoryScoresByDate = getScoresInRange(startDate, endDate);
    LOG.debug("categoryScoresByDate={}", categoryScoresByDate);
    
    if (isMoreThanMonth(startDate, endDate)) {
      var x = groupByWeek(categoryScoresByDate);
      
      return x;
    } else {
      return categoryScoresByDate;
    }
  }
  
  private boolean isMoreThanMonth(LocalDate startDate, LocalDate endDate) {
    boolean moreThanMonth = startDate.plusMonths(1).isBefore(endDate);
    
    LOG.debug("Difference in startDate={} and endDate={} is moreThanMonth={}", startDate, endDate, moreThanMonth);
    return moreThanMonth;
  }
  
  public List<CategoryScoreSummary> groupByWeek(List<CategoryScoreSummary> categoryScoreSummaries) {
    var merged = new ArrayList<CategoryScoreSummary>();
    
    for (CategoryScoreSummary currC : categoryScoreSummaries) {
      // Step 1: Group timeline per week
      var mapTimelinePerWeek = currC.timeline().stream()
          .collect(Collectors.groupingBy(
              it -> it.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
              Collectors.toList()
          ));
      
      // Step 2: Calculate score average of the week
      // and create new timeline in new Category summary.
      var scoreSum = BigDecimal.ZERO;
      var count = 0L;
      var timelinePerWeek = new ArrayList<CategoryScoreSummary.Timeline>();
      
      for (var weekEntry : mapTimelinePerWeek.entrySet()) {
        var weekStartDate = weekEntry.getKey();
        var listOfDaysToMerge = weekEntry.getValue();
        
        for (var value : listOfDaysToMerge) {
          count += 1;
          scoreSum = scoreSum.add(value.score());
        }
        var scoreAverage = scoreSum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN);
        
        timelinePerWeek.add(CategoryScoreSummary.Timeline.of(weekStartDate, scoreAverage));
      }
      
      merged.add(CategoryScoreSummary.of(
          currC.categoryId(),
          currC.ratingsCount(),
          currC.averageScore(),
          timelinePerWeek
      ));
    }
    
    return categoryScoreSummaries;
  }
  
  private List<CategoryScoreSummary> getScoresInRange(LocalDate startDate, LocalDate endDate) {
    var categoryScoreStatsMap = startDate.datesUntil(endDate.plusDays(1))
        .map(date -> cacheStore.getOrCalculate(date, this::calculate))
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .collect(Collectors.groupingBy(CategoryScoreStatsByRatingDate::categoryId, Collectors.toList()));
    
    var categoryScoreSummaries = categoryScoreStatsMap.entrySet().stream()
        .map(e -> {
          var categoryId = e.getKey();
          var ratingsCount = 0L;
          var scoreSum = BigDecimal.ZERO;
          var count = 0L;
          var timeline = new ArrayList<CategoryScoreSummary.Timeline>();
          
          // Step 4: Compute average of scores per category
          for (var categoryScoreStats : e.getValue()) {
            count += 1;
            ratingsCount += categoryScoreStats.ratingCount();
            scoreSum = scoreSum.add(categoryScoreStats.scoreAverage());
            timeline.add(CategoryScoreSummary.Timeline.of(categoryScoreStats.ratingDate(), categoryScoreStats.scoreAverage()));
          }
          var scoreAverage = count > 0 ? scoreSum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
          
          var out = CategoryScoreSummary.of(
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
