package com.shishir.ticketmetrics.integration.grpc;


import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.annotation.IntegrationTest;
import com.shishir.ticketmetrics.testsupport.utl.CacheTestUtil;
import com.shishir.ticketmetrics.testsupport.utl.GrpcTestUtil;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.grpc.test.LocalGrpcPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
@IntegrationTest
@Sql(scripts = {"/sql/schema.sql", "/sql/test_data_compare_period_scores.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ComparePeriodScoresTest {
  
  @LocalGrpcPort
  private int port;
  
  @Autowired
  private CacheManager cacheManager;
  
  private ManagedChannel channel;
  private TicketMetricsServiceGrpc.TicketMetricsServiceBlockingStub grpcStub;
  
  @BeforeEach
  void setup() {
    channel = GrpcTestUtil.buildManagedChannel(port);
    grpcStub = GrpcTestUtil.buildServiceStub(channel);
  }
  
  @AfterEach
  void shutdown() {
    channel.shutdownNow();
    CacheTestUtil.clearCache(cacheManager);
  }
  
  
  @Test
  void shouldFail_whenEmptyRequest() {
    assertThatThrownBy(() -> grpcStub.getCategoryTimelineScores(null))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must not be blank");
  }
  
  @Test
  void shouldFail_whenInvalidCurrentStartDate() {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest(
        "incorrect-start-date",
        "2025-07-04T00:00:00",
        "2025-06-04T00:00:00",
        "2025-06-04T00:00:00"
    );
    
    assertThatThrownBy(() -> grpcStub.comparePeriodScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: current_start_date must be in ISO date-time format but was incorrect-start-date");
  }
  
  @Test
  void shouldFail_whenInvalidCurrentEndDate() {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest(
        "2025-07-04T00:00:00",
        "incorrect-end-date",
        "2025-06-04T00:00:00",
        "2025-06-04T00:00:00"
    );
    
    assertThatThrownBy(() -> grpcStub.comparePeriodScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: current_end_date must be in ISO date-time format but was incorrect-end-date");
  }
  
  @Test
  void shouldFail_whenInvalidDateOrder() {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest(
        "2025-07-04T00:00:00",
        "2025-06-04T00:00:00",
        "2025-05-04T00:00:00",
        "2025-04-04T00:00:00"
    );
    
    assertThatThrownBy(() -> grpcStub.comparePeriodScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: End date must not be before startDate date");
  }
  
  @ParameterizedTest(name = "{index} => scenario={0}")
  @MethodSource("getComparePeriodScoresTestData")
  void canComparePeriodScores(
      String scenario,
      String currentStartDate,
      String currentEndDate,
      String previousStartDate,
      String previousEndDate,
      double expectedCurrentPeriodScore,
      double expectedPreviousPeriodScore,
      double expectedScoreChange
  ) {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest(currentStartDate, currentEndDate, previousStartDate, previousEndDate);
    var response = grpcStub.comparePeriodScores(request);
    
    assertThat(response.getCurrentPeriodScore()).isEqualTo(expectedCurrentPeriodScore);
    assertThat(response.getPreviousPeriodScore()).isEqualTo(expectedPreviousPeriodScore);
    assertThat(response.getScoreChange()).isEqualTo(expectedScoreChange);
  }
  
  static Stream<Arguments> getComparePeriodScoresTestData() {
    return Stream.of(
        arguments("07/01 <-> 07/01", "2025-07-01T00:00:00", "2025-07-01T00:00:00", "2025-07-01T00:00:00", "2025-07-01T00:00:00", 89.09, 89.09, 0.0),
        arguments("07/02 <-> 07/02", "2025-07-02T00:00:00", "2025-07-02T00:00:00", "2025-07-02T00:00:00", "2025-07-02T00:00:00", 48.24, 48.24, 0.0),
        arguments("07/03 <-> 07/03", "2025-07-03T00:00:00", "2025-07-03T00:00:00", "2025-07-03T00:00:00", "2025-07-03T00:00:00", 88.24, 88.24, 0.0),
        arguments("07/04 <-> 07/04", "2025-07-04T00:00:00", "2025-07-04T00:00:00", "2025-07-04T00:00:00", "2025-07-04T00:00:00", 90.91, 90.91, 0.0),
        // 07/03-07/04 to 07/01-07/02
        // 07/03-07/04 = (88.24 + 90.91) / 2 = 179.15 / 2 = 89.58
        // 07/01-07/02 = (89.09 + 48.24) / 2 = 137.33 / 2 = 68.67
        arguments("07/03-07/04 <-> 07/01-07/02", "2025-07-03T00:00:00", "2025-07-04T00:00:00", "2025-07-01T00:00:00", "2025-07-02T00:00:00", 89.58, 68.67, 20.91),
        // 07/02-07/04 to 07/01-07/01
        // 07/02-07/04 = (48.24 + 88.24 + 90.91) / 3 = 227.39 / 3 = 75.80
        arguments("07/02-07/04 <-> 07/01-07/01", "2025-07-02T00:00:00", "2025-07-04T00:00:00", "2025-07-01T00:00:00", "2025-07-01T00:00:00", 75.80, 89.09, -13.29)
    );
  }
}
