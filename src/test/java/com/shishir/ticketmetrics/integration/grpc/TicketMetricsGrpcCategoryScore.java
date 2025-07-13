package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.generated.grpc.CategoryScore;
import com.shishir.ticketmetrics.generated.grpc.CategoryScoreRequest;
import com.shishir.ticketmetrics.generated.grpc.CategoryScoreResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.IntegrationTest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.LocalGrpcPort;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@IntegrationTest
@Sql(scripts = {"/sql/schema.sql", "/sql/data_category_timeline.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TicketMetricsGrpcCategoryScore {
  @LocalGrpcPort
  int port;
  
  private ManagedChannel channel;
  private TicketMetricsServiceGrpc.TicketMetricsServiceBlockingStub stub;
  
  @BeforeEach
  void setup() {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext()
        .build();
    stub = TicketMetricsServiceGrpc.newBlockingStub(channel);
  }
  
  @AfterEach
  void shutdown() {
    channel.shutdownNow();
  }
  
  @Test
  void testGetCategoryScoreOverTime() {
    CategoryScoreRequest request = CategoryScoreRequest.newBuilder()
        .setStartDate("2025-07-02T00:00:00")
        .setEndDate("2025-07-04T00:00:00")
        .build();
    
    CategoryScoreResponse response = stub.getCategoryTimelineScores(request);
    
    assertThat(response.getScoresList()).isNotEmpty();
    for (CategoryScore score : response.getScoresList()) {
      assertThat(score.getCategoryId()).isGreaterThan(0);
      assertThat(score.getAverageScore()).isBetween(0.0, 100.0);
    }
  }
}
