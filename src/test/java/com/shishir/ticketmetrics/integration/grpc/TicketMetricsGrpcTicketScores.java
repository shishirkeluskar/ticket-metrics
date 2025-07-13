package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.generated.grpc.TicketScoreRequest;
import com.shishir.ticketmetrics.generated.grpc.TicketScoreResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketScoreRow;
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
@Sql(scripts = {"/sql/schema.sql", "/sql/data_ticket_matrix.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TicketMetricsGrpcTicketScores {
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
        .setStart("2025-07-01T00:00:00")
        .setEnd("2025-07-02T00:00:00")
        .build();
    
    TicketScoreResponse response = stub.getTicketScores(request);
    
    assertThat(response.getTicketScoresList()).isNotEmpty();
    for (TicketScoreRow row : response.getTicketScoresList()) {
      assertThat(row.getTicketId()).isGreaterThan(0);
      assertThat(row.getCategoryScoresMap()).isNotEmpty();
    }
  }
}
