package com.shishir.ticketmetrics.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.LocalGrpcPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TicketMetricsGrpcCategoryScoreIT {
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
        .setStartDate("2019-02-25T00:00:00")
        .setEndDate("2019-03-25T00:00:00")
        .build();
    
    CategoryScoreResponse response = stub.getCategoryScoreOverTime(request);
    
    assertThat(response.getScoresList()).isNotEmpty();
    for (CategoryScore score : response.getScoresList()) {
      assertThat(score.getCategoryId()).isGreaterThan(0);
      assertThat(score.getAverageScore()).isBetween(0.0, 100.0);
    }
  }
}
