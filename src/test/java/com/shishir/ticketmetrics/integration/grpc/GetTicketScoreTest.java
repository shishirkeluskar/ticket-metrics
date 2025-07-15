package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.annotation.IntegrationTest;
import com.shishir.ticketmetrics.testsupport.utl.CacheTestUtil;
import com.shishir.ticketmetrics.testsupport.utl.GrpcTestUtil;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.grpc.test.LocalGrpcPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
@IntegrationTest
@Sql(scripts = {"/sql/schema.sql", "/sql/test_data_get_ticket_score.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GetTicketScoreTest {
  
  @LocalGrpcPort
  int port;
  
  @Autowired
  private CacheManager cacheManager;
  
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
    CacheTestUtil.clearCache(cacheManager);
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
  
  @ParameterizedTest()
  @MethodSource("getTicketScoreTestData")
  void canGetTicketScore(Integer ticketId, double expectedScore) {
    var request = GrpcTestUtil.buildGetTicketScoreRequest(ticketId);
    
    var response = grpcStub.getTicketScore(request);
    
    assertThat(response.getScore()).isEqualTo(expectedScore);
  }
  
  static Stream<Arguments> getTicketScoreTestData() {
    return Stream.of(
        arguments(201, 89d),
        arguments(202, 48d),
        arguments(203, 93d)
    );
  }
}

