package com.shishir.ticketmetrics.integration.grpc;


import com.shishir.ticketmetrics.generated.grpc.PeriodScoreComparisonRequest;
import com.shishir.ticketmetrics.generated.grpc.PeriodScoreComparisonResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.annotation.IntegrationTest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
class TicketMetricsGrpcPeriodOverPeriod {
  
  @LocalGrpcPort
  private int port;
  private ManagedChannel channel;
  private TicketMetricsServiceGrpc.TicketMetricsServiceBlockingStub stub;
  
  @BeforeEach
  void setup() {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext()
        .build();
    stub = TicketMetricsServiceGrpc.newBlockingStub(channel);
  }
  
  
  @Test
  void testGetPeriodOverPeriodScoreChange() {
    String currentStart = "2020-01-01T00:00:00";
    String currentEnd = "2020-01-31T00:00:00";
    
    PeriodScoreComparisonRequest request = PeriodScoreComparisonRequest.newBuilder()
        .setCurrentStart(currentStart)
        .setCurrentEnd(currentEnd)
        .build();
    
    PeriodScoreComparisonResponse response = stub.comparePeriodScores(request);
    
    assertThat(response.getCurrentPeriodScore()).isGreaterThanOrEqualTo(0);
    assertThat(response.getPreviousPeriodScore()).isGreaterThanOrEqualTo(0);
  }
}
