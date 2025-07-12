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
class TicketMetricsGrpcTicketScoreIT {
  
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
  void testGetTicketScore_returnsScore() {
    GetTicketScoreRequest request = GetTicketScoreRequest.newBuilder()
        .setTicketId(1) // Make sure ticket ID 1 exists in your test DB
        .build();
    
    GetTicketScoreResponse response = stub.getTicketScore(request);
    
    assertThat(response.getScore()).isBetween(0.0, 100.0);
  }
}
