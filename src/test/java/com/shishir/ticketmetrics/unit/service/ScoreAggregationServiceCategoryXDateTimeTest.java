package com.shishir.ticketmetrics.unit.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.model.RatingWithCategory;
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
class ScoreAggregationServiceCategoryXDateTimeTest {
  
  private RatingMapper ratingMapper;
  private ScoreAggregationService service;
  
  @BeforeEach
  void setUp() {
    ratingMapper = mock(RatingMapper.class);
    service = new ScoreAggregationService(ratingMapper);
  }
  
  @Test
  void testDailyAggregation_singleCategoryMultipleDays() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 3, 23, 59);
    
    List<RatingWithCategory> ratings = List.of(
        rating(1, 4, LocalDateTime.of(2025, 7, 1, 9, 0)),
        rating(1, 2, LocalDateTime.of(2025, 7, 1, 12, 0)),
        rating(1, 5, LocalDateTime.of(2025, 7, 2, 14, 30))
    );
    
    when(ratingMapper.findRatingsInRange(any(), any())).thenReturn(ratings);
    
    Map<Integer, CategoryScoreSummary> result = service.getCategoryScoresOverTime(start, end);
    
    assertThat(result).hasSize(1);
    CategoryScoreSummary summary = result.get(1);
    assertThat(summary.getTotalRatings()).isEqualTo(3);
    assertThat(summary.getDateScores()).hasSize(2);
    
    BigDecimal firstDayScore = summary.getDateScores().get(LocalDateTime.of(2025, 7, 1, 0, 0));
    BigDecimal secondDayScore = summary.getDateScores().get(LocalDateTime.of(2025, 7, 2, 0, 0));
    
    assertThat(firstDayScore).isEqualByComparingTo(BigDecimal.valueOf(60.00));
    assertThat(secondDayScore).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    assertThat(summary.getFinalAverageScore()).isEqualByComparingTo(BigDecimal.valueOf(73.33));
  }
  
  private RatingWithCategory rating(int categoryId, int value, LocalDateTime ts) {
    RatingWithCategory r = new RatingWithCategory(
        categoryId,
        BigDecimal.valueOf(value),
        ts
    );
    return r;
  }
}
