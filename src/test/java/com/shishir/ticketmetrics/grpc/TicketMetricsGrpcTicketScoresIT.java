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
public class TicketMetricsGrpcTicketScoresIT {
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
  void testGetTicketScores() {
    TicketScoreRequest request = TicketScoreRequest.newBuilder()
        .setStart("2019-03-01T00:00:00")
        .setEnd("2019-03-31T00:00:00")
        .build();
    
    TicketScoreResponse response = stub.getTicketScores(request);
    
    assertThat(response.getTicketScoresList()).isNotEmpty();
    for (TicketScoreRow row : response.getTicketScoresList()) {
      assertThat(row.getTicketId()).isGreaterThan(0);
      assertThat(row.getCategoryScoresMap()).isNotEmpty();
    }
  }
}
