package com.shishir.ticketmetrics.unit.service;

import com.shishir.ticketmetrics.mapper.RatingMapper;
import com.shishir.ticketmetrics.model.RatingWithCategoryWeight;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import com.shishir.ticketmetrics.testsupport.annotation.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
class ScoreAggregationServicePeriodOverPeriodTest {
  
  private RatingMapper ratingMapper;
  private ScoreAggregationService service;
  
  @BeforeEach
  void setUp() {
    ratingMapper = mock(RatingMapper.class);
    service = new ScoreAggregationService(ratingMapper);
  }
  
  @Test
  void testCalculatePeriodOverPeriodChange_normalCase() {
    LocalDateTime currentStart = LocalDateTime.of(2025, 7, 7, 0, 0);
    LocalDateTime currentEnd = LocalDateTime.of(2025, 7, 14, 0, 0); // 1 week
    LocalDateTime prevStart = LocalDateTime.of(2025, 6, 30, 0, 0);
    LocalDateTime prevEnd = LocalDateTime.of(2025, 7, 7, 0, 0);
    
    // 1 rating in current period (score = 80%)
    RatingWithCategoryWeight r1 = new RatingWithCategoryWeight(0, 0,
        BigDecimal.valueOf(4), BigDecimal.valueOf(2), currentStart.plusDays(1));
    
    // 1 rating in previous period (score = 60%)
    RatingWithCategoryWeight r2 = new RatingWithCategoryWeight(0, 0,
        BigDecimal.valueOf(3), BigDecimal.valueOf(2), prevStart.plusDays(1));
    
    when(ratingMapper.findRatingsCreatedBetween(eq(currentStart), eq(currentEnd)))
        .thenReturn(List.of(r1));
    
    when(ratingMapper.findRatingsCreatedBetween(eq(prevStart), eq(prevEnd)))
        .thenReturn(List.of(r2));
    
    var result = service.calculatePeriodOverPeriodChange(currentStart, currentEnd, prevStart, prevEnd);
    
    // Current: (4*2) / (2*5) * 100 = 80%
    // Previous: (3*2) / (2*5) * 100 = 60%
    // Change = 80 - 60 = 20
    
    assertThat(result.currentScore()).isEqualByComparingTo(BigDecimal.valueOf(80));
    assertThat(result.previousScore()).isEqualByComparingTo(BigDecimal.valueOf(60));
    assertThat(result.change()).isEqualByComparingTo(BigDecimal.valueOf(20));
  }
  
  @Test
  void testCalculatePeriodOverPeriodChange_withEmptyPeriods() {
    LocalDateTime currentStart = LocalDateTime.of(2025, 7, 7, 0, 0);
    LocalDateTime currentEnd = LocalDateTime.of(2025, 7, 14, 0, 0);
    LocalDateTime prevStart = LocalDateTime.of(2025, 6, 7, 0, 0);
    LocalDateTime prevEnd = LocalDateTime.of(2025, 6, 14, 0, 0);
    
    when(ratingMapper.findRatingsCreatedBetween(any(), any()))
        .thenReturn(List.of()); // both current and previous return empty
    
    var result = service.calculatePeriodOverPeriodChange(currentStart, currentEnd, prevStart, prevEnd);
    
    assertThat(result.currentScore()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.previousScore()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.change()).isEqualByComparingTo(BigDecimal.ZERO);
  }
}
