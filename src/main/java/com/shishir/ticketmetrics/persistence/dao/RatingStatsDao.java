package com.shishir.ticketmetrics.persistence.dao;

import com.shishir.ticketmetrics.persistence.dto.CategoryRatingStatsByRatingDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RatingStatsDao {
  /**
   * Fetches aggregated rating statistics for each category on a specific rating date.
   *
   * @param date the date to group ratings by (`ratings.created_at`)
   * @return list of aggregated stats per category
   */
  @Select("""
          SELECT
            rating_category_id AS categoryId,
            DATE(created_at) AS ratingDate,
            COUNT(*) AS ratingCount,
            SUM(rating) AS ratingSum,
            AVG(rating) AS averageRating
          FROM ratings
          WHERE DATE(created_at) = #{date}
          GROUP BY rating_category_id
          ORDER BY rating_category_id
      """)
  List<CategoryRatingStatsByRatingDate> fetchCategoryStatsByRatingDate(@Param("date") LocalDate date);
}
