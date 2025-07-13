package com.shishir.ticketmetrics.integration.grpc;


import com.shishir.ticketmetrics.generated.grpc.PeriodScoreComparisonResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.annotation.IntegrationTest;
import com.shishir.ticketmetrics.testsupport.utl.GrpcTestUtil;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.LocalGrpcPort;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

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
  void testGetPeriodOverPeriodScoreChange() {
    String currentStart = "2020-01-01T00:00:00";
    String currentEnd = "2020-01-31T00:00:00";
    
    var request = GrpcTestUtil.buildComparePeriodScoresRequest(currentStart, currentEnd);
    
    PeriodScoreComparisonResponse response = grpcStub.comparePeriodScores(request);
    
    assertThat(response.getCurrentPeriodScore()).isGreaterThanOrEqualTo(0);
    assertThat(response.getPreviousPeriodScore()).isGreaterThanOrEqualTo(0);
  }
}
