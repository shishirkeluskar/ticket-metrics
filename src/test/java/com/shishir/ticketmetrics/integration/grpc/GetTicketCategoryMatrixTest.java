package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.generated.grpc.TicketCategoryMatrixResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketCategoryScoreRow;
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
@Sql(scripts = {"/sql/schema.sql", "/sql/data_ticket_matrix.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GetTicketCategoryMatrixTest {
  
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
  void testGetTicketScores() {
    var startDate = "2025-07-01T00:00:00";
    var endDate = "2025-07-02T00:00:00";
    
    var request = GrpcTestUtil.buildGetTicketCategoryMatrixRequest(startDate, endDate);
    
    TicketCategoryMatrixResponse response = grpcStub.getTicketCategoryMatrix(request);
    
    assertThat(response.getTicketScoresList()).isNotEmpty();
    for (TicketCategoryScoreRow row : response.getTicketScoresList()) {
      assertThat(row.getTicketId()).isGreaterThan(0);
      assertThat(row.getCategoryScoresMap()).isNotEmpty();
    }
  }
}
