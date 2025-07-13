package com.shishir.ticketmetrics.integration.grpc;

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
@Sql(scripts = {"/sql/schema.sql", "/sql/data_ticket_score.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GetTicketScoreTest {
  
  @LocalGrpcPort
  int port;
  
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
    assertThatThrownBy(() -> grpcStub.getTicketScore(null))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: ticket_id must be a positive number");
  }
  
  @Test
  void shouldFail_whenTicketIdInvalid() {
    var request = GrpcTestUtil.buildGetTicketScoreRequest(0);
    
    assertThatThrownBy(() -> grpcStub.getTicketScore(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: ticket_id must be a positive number");
  }
  
  @Test
  void canGetTicketScore() {
    var request = GrpcTestUtil.buildGetTicketScoreRequest(1);
    
    var response = grpcStub.getTicketScore(request);
    
    assertThat(response.getScore()).isBetween(0.0, 100.0);
  }
}

