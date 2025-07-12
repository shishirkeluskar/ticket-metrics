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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
class ScoreAggregationServiceTicketXDateTimeTest {
  
  private RatingMapper ratingMapper;
  private ScoreAggregationService service;
  
  @BeforeEach
  void setUp() {
    ratingMapper = mock(RatingMapper.class);
    service = new ScoreAggregationService(ratingMapper);
  }
  
  @Test
  void testGetScoresByTicket_singleRating() {
    RatingWithCategoryWeight rating = new RatingWithCategoryWeight(
        1,
        10,
        BigDecimal.valueOf(4),
        BigDecimal.valueOf(2),
        LocalDateTime.of(2025, 7, 1, 10, 0)
    );
    
    when(ratingMapper.findRatingsForTicketsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of(rating));
    
    when(ratingMapper.findRatingsForTicketCategoryBetween(anyInt(), anyInt(), any(), any()))
        .thenAnswer(invocation -> {
          int ticketId = invocation.getArgument(0);
          int categoryId = invocation.getArgument(1);
          if (ticketId == 1 && categoryId == 10) {
            return List.of(new RatingWithCategoryWeight(
                1, 10, BigDecimal.valueOf(4), BigDecimal.valueOf(2),
                LocalDateTime.of(2025, 7, 1, 10, 0)
            ));
          }
          return List.of();
        });
    
    Map<Integer, Map<Integer, BigDecimal>> result = service.getScoresByTicket(
        LocalDateTime.of(2025, 7, 1, 0, 0),
        LocalDateTime.of(2025, 7, 2, 0, 0)
    );
    
    BigDecimal expected = BigDecimal.valueOf(4 * 2 * 100).divide(BigDecimal.valueOf(2 * 5), 2);
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(1).get(10)).isEqualByComparingTo(expected);
  }
  
  @Test
  void testGetScoresByTicket_multipleRatingsDifferentCategories() {
    RatingWithCategoryWeight r1 = new RatingWithCategoryWeight(
        2,
        1,
        BigDecimal.valueOf(3),
        BigDecimal.valueOf(1),
        LocalDateTime.of(2025, 7, 1, 10, 0)
    );
    
    RatingWithCategoryWeight r2 = new RatingWithCategoryWeight(
        2,
        2,
        BigDecimal.valueOf(5),
        BigDecimal.valueOf(2),
        LocalDateTime.of(2025, 7, 1, 10, 0)
    );
    
    when(ratingMapper.findRatingsForTicketsCreatedBetween(any(), any()))
        .thenReturn(List.of(r1, r2));
    
    when(ratingMapper.findRatingsForTicketCategoryBetween(anyInt(), anyInt(), any(), any()))
        .thenAnswer(invocation -> {
          int ticketId = invocation.getArgument(0);
          int categoryId = invocation.getArgument(1);
          if (ticketId == 2 && categoryId == 1) {
            return List.of(new RatingWithCategoryWeight(
                2, 1, BigDecimal.valueOf(3), BigDecimal.valueOf(1),
                LocalDateTime.of(2025, 7, 1, 10, 0)
            ));
          }
          if (ticketId == 2 && categoryId == 2) {
            return List.of(new RatingWithCategoryWeight(
                2, 2, BigDecimal.valueOf(5), BigDecimal.valueOf(2),
                LocalDateTime.of(2025, 7, 1, 10, 0)
            ));
          }
          return List.of();
        });
    
    Map<Integer, Map<Integer, BigDecimal>> result = service.getScoresByTicket(
        LocalDateTime.now().minusDays(2),
        LocalDateTime.now()
    );
    
    BigDecimal score1 = BigDecimal.valueOf(3 * 1 * 100).divide(BigDecimal.valueOf(1 * 5), 2);
    BigDecimal score2 = BigDecimal.valueOf(5 * 2 * 100).divide(BigDecimal.valueOf(2 * 5), 2);
    
    assertThat(result.get(2).get(1)).isEqualByComparingTo(score1);
    assertThat(result.get(2).get(2)).isEqualByComparingTo(score2);
  }
}
