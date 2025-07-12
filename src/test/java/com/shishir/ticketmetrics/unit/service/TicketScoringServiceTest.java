package com.shishir.ticketmetrics.unit.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.Rating;
import com.shishir.ticketmetrics.service.RatingCategoryService;
import com.shishir.ticketmetrics.service.TicketScoringService;
import com.shishir.ticketmetrics.testsupport.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
class TicketScoringServiceTest {
  
  private RatingMapper ratingMapper;
  private RatingCategoryService categoryService;
  private TicketScoringService scoringService;
  
  @BeforeEach
  void setup() {
    ratingMapper = mock(RatingMapper.class);
    categoryService = mock(RatingCategoryService.class);
    scoringService = new TicketScoringService(ratingMapper, categoryService);
  }
  
  @Test
  void testComputeScore() {
    int ticketId = 1;
    
    List<Rating> ratings = List.of(
        createRating(1, BigDecimal.valueOf(4)),
        createRating(2, BigDecimal.valueOf(5))
    );
    
    Map<Integer, BigDecimal> weights = Map.of(
        1, BigDecimal.valueOf(2),
        2, BigDecimal.valueOf(3)
    );
    
    when(ratingMapper.findRatingsByTicketId(ticketId)).thenReturn(ratings);
    when(categoryService.getCategoryWeightMap()).thenReturn(weights);
    
    double score = scoringService.computeScore(ticketId);
    
    assertEquals(92.0, score); // ((80x2 + 100x3) / 5) = 92%
  }
  
  private Rating createRating(Integer categoryId, BigDecimal rating) {
    Rating r = new Rating(
        null,
        rating,
        null,
        categoryId,
        null,
        null,
        LocalDateTime.now()
    );
    return r;
  }
}
