package com.shishir.ticketmetrics.service;

import com.shishir.ticketmetrics.mapper.RatingCategoryMapper;
import com.shishir.ticketmetrics.mapper.TicketRatingMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketScoreService {
  
//  private final RatingCategoryMapper ratingCategoryMapper;
//  private final TicketRatingMapper ticketRatingMapper;
//
//  public TicketScoreService(RatingCategoryMapper ratingCategoryMapper,
//                            TicketRatingMapper ticketRatingMapper) {
//    this.ratingCategoryMapper = ratingCategoryMapper;
//    this.ticketRatingMapper = ticketRatingMapper;
//  }
//
//  public Map<Integer, Double> getTicketScores(String startDate, String endDate) {
//    List<TicketRatingMapper.TicketRating> ratings = ticketRatingMapper.findRatingsInPeriod(startDate, endDate);
//    Map<Integer, Integer> categoryWeights = ratingCategoryMapper.getAllCategoryWeights();
//
//    // Group ratings: ticketId → (categoryId → list of ratings)
//    Map<Integer, Map<Integer, List<Integer>>> ticketGroupedRatings = new HashMap<>();
//
//    for (TicketRatingMapper.TicketRating rating : ratings) {
//      ticketGroupedRatings
//          .computeIfAbsent(rating.ticketId(), k -> new HashMap<>())
//          .computeIfAbsent(rating.categoryId(), k -> new ArrayList<>())
//          .add(rating.rating());
//    }
//
//    // Calculate score per ticket
//    Map<Integer, Double> ticketScores = new HashMap<>();
//
//    for (Map.Entry<Integer, Map<Integer, List<Integer>>> ticketEntry : ticketGroupedRatings.entrySet()) {
//      int ticketId = ticketEntry.getKey();
//      Map<Integer, List<Integer>> categoryRatings = ticketEntry.getValue();
//
//      double score = TicketScoreCalculator.calculateScore(categoryRatings, categoryWeights);
//      ticketScores.put(ticketId, score);
//    }
//
//    return ticketScores;
//  }
}
