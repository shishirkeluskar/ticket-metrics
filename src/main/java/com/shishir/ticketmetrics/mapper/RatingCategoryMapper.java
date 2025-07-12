package com.shishir.ticketmetrics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface RatingCategoryMapper {
  
  @Select("SELECT id, weight FROM rating_categories")
  Map<Integer, Integer> getAllCategoryWeights();
}
