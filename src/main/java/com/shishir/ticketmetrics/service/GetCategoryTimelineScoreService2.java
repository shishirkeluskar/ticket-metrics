package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.fn.CategoryScoreByRatingDateCalculator;
import com.shishir.ticketmetrics.cache.store.CategoryScoreByRatingDateCacheStore;
import com.shishir.ticketmetrics.model.CategoryRatingStatsByRatingDateDto;
import com.shishir.ticketmetrics.model.CategoryScoreByRatingDateWrapper;
import com.shishir.ticketmetrics.persistence.dao.RatingStatsDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GetCategoryTimelineScoreService2 implements CategoryScoreByRatingDateCalculator {
  private final CategoryScoreByRatingDateCacheStore cacheStore;
  private final RatingStatsDao ratingStatsDao;
  
  public GetCategoryTimelineScoreService2(CategoryScoreByRatingDateCacheStore cacheStore, RatingStatsDao ratingStatsDao) {
    this.cacheStore = cacheStore;
    this.ratingStatsDao = ratingStatsDao;
  }
  
  public List<CategoryScoreByRatingDateWrapper> getCategoryTimelineScores(LocalDate startDate, LocalDate endDate) {
    return List.of();
  }
  
  private List<CategoryRatingStatsByRatingDateDto> getScoresInRange(LocalDate startDate, LocalDate endDate) {
    return startDate.datesUntil(endDate.plusDays(1))
        .map(date -> cacheStore.getOrCalculate(date, this::calculate))
        .toList();
  }
  
  @Override
  public CategoryRatingStatsByRatingDateDto calculate(LocalDate date) {
    return null;
  }
}
