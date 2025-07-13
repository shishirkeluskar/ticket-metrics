package com.shishir.ticketmetrics.integration.grpc;


import com.shishir.ticketmetrics.generated.grpc.PeriodScoreComparisonResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.annotation.IntegrationTest;
import com.shishir.ticketmetrics.testsupport.utl.GrpcTestUtil;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.LocalGrpcPort;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@IntegrationTest
@Sql(scripts = {"/sql/schema.sql", "/sql/data_period_comparison.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ComparePeriodScoresTest {
  
  @LocalGrpcPort
  private int port;
  
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
  }
  
  @Test
  void shouldFail_whenEmptyRequest() {
    assertThatThrownBy(() -> grpcStub.getCategoryTimelineScores(null))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must not be blank");
  }
  
  @Test
  void shouldFail_whenInvalidCurrentStartDate() {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest("incorrect-start-date", "2025-07-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.comparePeriodScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: current_start_date must be in ISO date-time format but was incorrect-start-date");
  }
  
  @Test
  void shouldFail_whenInvalidCurrentEndDate() {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest("2025-07-04T00:00:00", "incorrect-end-date");
    
    assertThatThrownBy(() -> grpcStub.comparePeriodScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: current_end_date must be in ISO date-time format but was incorrect-end-date");
  }
  
  @Test
  void shouldFail_whenInvalidDateOrder() {
    var request = GrpcTestUtil.buildComparePeriodScoresRequest("2025-07-04T00:00:00", "2025-06-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.comparePeriodScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: End date must not be before startDate date");
  }
  
  @Test
  void testGetPeriodOverPeriodScoreChange() {
    String currentStart = "2020-01-01T00:00:00";
    String currentEnd = "2020-01-31T00:00:00";
    
    var request = GrpcTestUtil.buildComparePeriodScoresRequest(currentStart, currentEnd);
    
    PeriodScoreComparisonResponse response = grpcStub.comparePeriodScores(request);
    
    assertThat(response.getCurrentPeriodScore()).isGreaterThanOrEqualTo(0);
    assertThat(response.getPreviousPeriodScore()).isGreaterThanOrEqualTo(0);
  }
}
