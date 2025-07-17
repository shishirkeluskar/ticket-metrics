package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.cache.fn.CategoryScoreByRatingDateCalculator;
import com.shishir.ticketmetrics.cache.store.CategoryScoreByRatingDateCacheStore;
import com.shishir.ticketmetrics.model.CategoryScoreByRatingDate;
import com.shishir.ticketmetrics.model.CategoryScoreByRatingDateWrapper;
import com.shishir.ticketmetrics.persistence.dao.RatingDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GetCategoryTimelineScoreService2 implements CategoryScoreByRatingDateCalculator {
  private final CategoryScoreByRatingDateCacheStore cacheStore;
  private final RatingDao ratingDao;
  
  public GetCategoryTimelineScoreService2(CategoryScoreByRatingDateCacheStore cacheStore, RatingDao ratingDao) {
    this.cacheStore = cacheStore;
    this.ratingDao = ratingDao;
  }
  
  public List<CategoryScoreByRatingDateWrapper> getCategoryTimelineScores(LocalDate startDate, LocalDate endDate) {
    return List.of();
  }
  
  @Override
  public CategoryScoreByRatingDate calculate(LocalDate date) {
    return null;
  }
}
