package com.shishir.ticketmetrics.mapper;

import com.shishir.ticketmetrics.model.Rating;
import com.shishir.ticketmetrics.model.RatingCategory;
import com.shishir.ticketmetrics.model.RatingWithCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RatingMapper {
  @Select("""
          SELECT *
          FROM ratings
          WHERE ticket_id = #{ticketId}
      """)
  List<Rating> findRatingsByTicketId(@Param("ticketId") Integer ticketId);
  
  @Select("""
          SELECT id,
                 name,
                 weight
          FROM rating_categories
      """)
  List<RatingCategory> findAllRatingCategories();
  
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
}
