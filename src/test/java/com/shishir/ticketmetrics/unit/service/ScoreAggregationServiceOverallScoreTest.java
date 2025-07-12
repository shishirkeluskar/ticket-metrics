package com.shishir.ticketmetrics.unit.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.RatingWithCategoryWeight;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import com.shishir.ticketmetrics.testsupport.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
class ScoreAggregationServiceOverallScoreTest {
  
  private RatingMapper ratingMapper;
  private ScoreAggregationService service;
  
  @BeforeEach
  void setUp() {
    ratingMapper = mock(RatingMapper.class);
    service = new ScoreAggregationService(ratingMapper);
  }
  
  @Test
  void testGetOverallScore_noRatings() {
    when(ratingMapper.findRatingsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of());
    
    BigDecimal score = service.getOverallScore(LocalDateTime.now().minusDays(7), LocalDateTime.now());
    assertThat(score).isEqualByComparingTo(BigDecimal.ZERO);
  }
  
  @Test
  void testGetOverallScore_singleRating() {
    RatingWithCategoryWeight rating = new RatingWithCategoryWeight(
        0, // id (not used here)
        0, // ticketId (not used here)
        BigDecimal.valueOf(4),
        BigDecimal.valueOf(2),
        LocalDateTime.now()
    );
    
    when(ratingMapper.findRatingsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of(rating));
    
    // Expected: (4 * 2) / (2 * 5) * 100 = 80.00
    BigDecimal expected = BigDecimal.valueOf(80).setScale(2);
    
    BigDecimal score = service.getOverallScore(LocalDateTime.now().minusDays(7), LocalDateTime.now());
    assertThat(score).isEqualByComparingTo(expected);
  }
  
  @Test
  void testGetOverallScore_multipleRatings() {
    RatingWithCategoryWeight r1 = new RatingWithCategoryWeight(
        0, 0,
        BigDecimal.valueOf(5),
        BigDecimal.valueOf(3),
        LocalDateTime.now()
    );
    
    RatingWithCategoryWeight r2 = new RatingWithCategoryWeight(
        0, 0,
        BigDecimal.valueOf(3),
        BigDecimal.valueOf(1),
        LocalDateTime.now()
    );
    
    when(ratingMapper.findRatingsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of(r1, r2));

        /*
         Calculation:
           weightedSum = (5*3) + (3*1) = 15 + 3 = 18
           totalWeight = (3*5) + (1*5) = 15 + 5 = 20
           score = (18 / 20) * 100 = 90.00
        */
    
    BigDecimal expected = BigDecimal.valueOf(90).setScale(2);
    
    BigDecimal score = service.getOverallScore(LocalDateTime.now().minusDays(7), LocalDateTime.now());
    assertThat(score).isEqualByComparingTo(expected);
  }
}
