package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
import com.shishir.ticketmetrics.testsupport.annotation.IntegrationTest;
import com.shishir.ticketmetrics.testsupport.utl.GrpcTestUtil;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.LocalGrpcPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
@IntegrationTest
@Sql(scripts = {"/sql/schema.sql", "/sql/test_data_category_timeline.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GetCategoryTimelineScoresTest {
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
    assertThatThrownBy(() -> grpcStub.getCategoryTimelineScores(null))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must not be blank");
  }
  
  @Test
  void shouldFail_whenInvalidStartDate() {
    var request = GrpcTestUtil.buildGetCategoryTimelineScoresRequest("incorrect-start-date", "2025-07-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.getCategoryTimelineScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must be in ISO date-time format but was incorrect-start-date");
  }
  
  @Test
  void shouldFail_whenInvalidEndDate() {
    var request = GrpcTestUtil.buildGetCategoryTimelineScoresRequest("2025-07-04T00:00:00", "incorrect-end-date");
    
    assertThatThrownBy(() -> grpcStub.getCategoryTimelineScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: end_date must be in ISO date-time format but was incorrect-end-date");
  }
  
  @Test
  void shouldFail_whenInvalidDateOrder() {
    var request = GrpcTestUtil.buildGetCategoryTimelineScoresRequest("2025-07-04T00:00:00", "2025-06-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.getCategoryTimelineScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: End date must not be before startDate date");
  }
  
  @ParameterizedTest
  @MethodSource("categoryTimelineCasesTestData")
  void canGetCategoryTimelineScores(String startDate, String endDate, Integer expectedCategoryCount, Integer expectedDatesPerCategory) {
    var request = GrpcTestUtil.buildGetCategoryTimelineScoresRequest(startDate, endDate);
    
    var response = grpcStub.getCategoryTimelineScores(request);
    
    assertThat(response.getScoresList()).hasSize(expectedCategoryCount);
    
    for (var category : response.getScoresList()) {
      assertThat(category.getTimelineList()).hasSize(expectedDatesPerCategory);
      assertThat(category.getAverageScore()).isBetween(0.0, 100.0);
      assertThat(category.getTotalRatings()).isGreaterThanOrEqualTo(0);
    }
  }
  
  static Stream<Arguments> categoryTimelineCasesTestData() {
    return Stream.of(
        arguments("2025-07-01T00:00:00", "2025-07-03T00:00:00", 2, 1), // daily
        arguments("2025-06-01T00:00:00", "2025-07-10T00:00:00", 3, 1)  // weekly
    );
  }
}
