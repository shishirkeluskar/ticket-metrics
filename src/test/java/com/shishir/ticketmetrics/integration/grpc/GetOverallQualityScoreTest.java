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
@Sql(scripts = {"/sql/schema.sql", "/sql/test_data_get_overall_quality_score.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GetOverallQualityScoreTest {
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
    assertThatThrownBy(() -> grpcStub.getOverallQualityScore(null))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must not be blank");
  }
  
  @Test
  void shouldFail_whenInvalidStartDate() {
    var request = GrpcTestUtil.buildOverallQualityScoreRequest("incorrect-start-date", "2025-07-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.getOverallQualityScore(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must be in ISO date-time format but was incorrect-start-date");
  }
  
  @Test
  void shouldFail_whenInvalidEndDate() {
    var request = GrpcTestUtil.buildOverallQualityScoreRequest("2025-07-04T00:00:00", "incorrect-end-date");
    
    assertThatThrownBy(() -> grpcStub.getOverallQualityScore(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: end_date must be in ISO date-time format but was incorrect-end-date");
  }
  
  @Test
  void shouldFail_whenInvalidDateOrder() {
    var request = GrpcTestUtil.buildOverallQualityScoreRequest("2025-07-04T00:00:00", "2025-06-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.getOverallQualityScore(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: End date must not be before startDate date");
  }
  
  @ParameterizedTest()
  @MethodSource("getOverallQualityScoreTestData")
  void canGetOverallQualityScore(String startDate, String endDate, double expectedScore) {
    var request = GrpcTestUtil.buildOverallQualityScoreRequest(startDate, endDate);
    
    var response = grpcStub.getOverallQualityScore(request);
    
    assertThat(response.getScore()).isEqualTo(expectedScore);
  }
  
  static Stream<Arguments> getOverallQualityScoreTestData() {
    return Stream.of(
        arguments("2025-07-01T00:00:00", "2025-07-01T00:00:00", 70d),
        arguments("2025-07-02T00:00:00", "2025-07-02T00:00:00", 93d),
        arguments("2025-07-01T00:00:00", "2025-07-02T00:00:00", 82d)
    );
  }
}
