package com.shishir.ticketmetrics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketRatingMapper {
  
  // DTO class to hold ticket_id, category_id, rating
  record TicketRating(int ticketId, int categoryId, int rating) {
  }
  
  @Select("""
          SELECT ticket_id, rating_category_id AS categoryId, rating
          FROM ratings
          WHERE created_at BETWEEN #{startDate} AND #{endDate}
      """)
  List<TicketRating> findRatingsInPeriod(
      @Param("startDate") String startDate, // use ISO format YYYY-MM-DD
      @Param("endDate") String endDate
  );
}
