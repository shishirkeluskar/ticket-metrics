package com.shishir.ticketmetrics.persistence.dao;

import com.shishir.ticketmetrics.model.RatingWithCategory;
import com.shishir.ticketmetrics.persistence.model.Rating;
import com.shishir.ticketmetrics.persistence.model.RatingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface RatingDao {
  @Select("""
          SELECT *
          FROM ratings
          WHERE ticket_id = #{ticketId}
      """)
  List<Rating> fetchRatingsByTicketId(@Param("ticketId") Integer ticketId);
  
  @Select("""
          SELECT id,
                 name,
                 weight
          FROM rating_categories
      """)
  @Cacheable("ratingCategories")
  List<RatingCategory> fetchRatingCategories();
  
  @Select("""
      SELECT t.id
      FROM tickets t
          JOIN ratings r ON t.id = r.ticket_id
      WHERE DATE(t.created_at) BETWEEN #{startDate} AND #{endDate}
      """)
  List<Integer> fetchRatedTickets(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
  
  @Select("""
          SELECT r.rating_category_id AS category_id,
                 r.rating,
                 r.created_at AS timestamp
          FROM ratings r
          WHERE r.created_at BETWEEN #{start} AND #{end}
      """)
  List<RatingWithCategory> findRatingsInRange(
      @Param("start") LocalDateTime startDate,
      @Param("end") LocalDateTime endDate
  );
  
  /**
   * Returns categoryId -> weight map.
   * This is cached because weights change rarely.
   */
  @Cacheable("categoryWeightMapById")
  default Map<Integer, BigDecimal> getCategoryWeightMap() {
    return fetchRatingCategories()
        .stream()
        .collect(Collectors.toMap(
            RatingCategory::id,
            RatingCategory::weight
        ));
  }
}
