package com.shishir.ticketmetrics.mapper;

import com.shishir.ticketmetrics.model.Rating;
import com.shishir.ticketmetrics.model.RatingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RatingMapper {
  @Select("""
          SELECT id,
                 ticket_id,
                 rating_category_id,
                 rating
          FROM ratings
          WHERE ticket_id = #{ticketId}
      """)
  List<Rating> findRatingsByTicketId(Integer ticketId);
  
  @Select("""
          SELECT id,
                 name,
                 weight
          FROM rating_categories
      """)
  List<RatingCategory> findAllRatingCategories();
}
