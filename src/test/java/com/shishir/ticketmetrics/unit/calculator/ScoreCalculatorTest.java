package com.shishir.ticketmetrics.unit.calculator;

import com.shishir.ticketmetrics.calculator.ScoreCalculator;
import com.shishir.ticketmetrics.testsupport.annotation.UnitTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
public class ScoreCalculatorTest {
  
  @Test
  void calculatesWeightedScoreCorrectly() {
    Map<Integer, BigDecimal> categoryRatings = Map.of(
        1, BigDecimal.valueOf(4), // Normalized to 80
        2, BigDecimal.valueOf(5) // Normalized to 100
    );
    
    Map<Integer, BigDecimal> categoryWeights = Map.of(
        1, BigDecimal.valueOf(2),
        2, BigDecimal.valueOf(3)
    );
    
    var result = ScoreCalculator.calculateScore(categoryRatings, categoryWeights);
    // Expected = ((80x2 + 100x3) / 5) = (160 + 300) /5 = 92.00
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(92.00));
  }
  
  @Test
  void returnsZeroWhenNoWeightsMatch() {
    Map<Integer, BigDecimal> categoryRatings = Map.of(
        1, BigDecimal.valueOf(5)
    );
    
    Map<Integer, BigDecimal> categoryWeights = Map.of(); // no weights provided
    
    var result = ScoreCalculator.calculateScore(categoryRatings, categoryWeights);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0.00));
  }
  
  
  @Test
  void ignoresUnknownCategoryIds() {
    Map<Integer, BigDecimal> categoryRatings = Map.of(
        1, BigDecimal.valueOf(3),
        2, BigDecimal.valueOf(5),
        999, BigDecimal.valueOf(4) // Unknown category
    );
    
    Map<Integer, BigDecimal> categoryWeights = Map.of(
        1, BigDecimal.valueOf(1),
        2, BigDecimal.valueOf(2)
    );
    
    var result = ScoreCalculator.calculateScore(categoryRatings, categoryWeights);
    // ((3/5)*100 * 1 + (5/5)*100 * 2) / (1+2) = (60 + 200) / 3 = 86.67
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(86.67));
  }
  
  @Test
  void returnsRoundedScoreToTwoDecimals() {
    Map<Integer, BigDecimal> categoryRatings = Map.of(
        1, BigDecimal.valueOf(2) // 40%
    );
    
    Map<Integer, BigDecimal> categoryWeights = Map.of(
        1, BigDecimal.valueOf(3)
    );
    
    var result = ScoreCalculator.calculateScore(categoryRatings, categoryWeights);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(40.0));
  }
}
