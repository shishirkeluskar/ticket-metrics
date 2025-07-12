package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.RatingCategory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RatingCategoryService {
  
  private final RatingMapper ratingMapper;
  
  public RatingCategoryService(RatingMapper ratingMapper) {
    this.ratingMapper = ratingMapper;
  }
  
  /**
   * Returns categoryId -> weight map.
   * This is cached because weights change rarely.
   */
  @Cacheable("ratingCategoriesById")
  public Map<Integer, BigDecimal> getCategoryWeightMap() {
    return getAllRatingCategories()
        .stream()
        .collect(Collectors.toMap(
            RatingCategory::id,
            RatingCategory::weight
        ));
  }
  
  @Cacheable("ratingCategories")
  public List<RatingCategory> getAllRatingCategories() {
    return ratingMapper.findAllRatingCategories();
  }
}
