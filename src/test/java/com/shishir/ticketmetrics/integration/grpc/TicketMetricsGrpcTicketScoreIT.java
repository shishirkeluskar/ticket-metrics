package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.grpc.GetTicketScoreRequest;
import com.shishir.ticketmetrics.grpc.GetTicketScoreResponse;
import com.shishir.ticketmetrics.grpc.TicketMetricsServiceGrpc;
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
@Sql(scripts = {"/sql/schema.sql", "/sql/data_ticket_score.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
        .setTicketId(1)
        .build();
    
    GetTicketScoreResponse response = stub.getTicketScore(request);
    
    assertThat(response.getScore()).isBetween(0.0, 100.0);
  }
}

