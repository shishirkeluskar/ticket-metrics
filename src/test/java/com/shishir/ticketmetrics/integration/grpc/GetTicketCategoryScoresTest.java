package com.shishir.ticketmetrics.integration.grpc;

import com.shishir.ticketmetrics.generated.grpc.GetTicketCategoryScoresResponse;
import com.shishir.ticketmetrics.generated.grpc.TicketCategoryScore;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
@IntegrationTest
@Sql(scripts = {"/sql/schema.sql", "/sql/test_data_get_ticket_category_scores.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GetTicketCategoryScoresTest {
  
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
    assertThatThrownBy(() -> grpcStub.getTicketCategoryScores(null))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must not be blank");
  }
  
  @Test
  void shouldFail_whenInvalidStartDate() {
    var request = GrpcTestUtil.buildGetTicketCategoryScoresRequest("incorrect-start-date", "2025-07-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.getTicketCategoryScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: start_date must be in ISO date-time format but was incorrect-start-date");
  }
  
  @Test
  void shouldFail_whenInvalidEndDate() {
    var request = GrpcTestUtil.buildGetTicketCategoryScoresRequest("2025-07-04T00:00:00", "incorrect-end-date");
    
    assertThatThrownBy(() -> grpcStub.getTicketCategoryScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: end_date must be in ISO date-time format but was incorrect-end-date");
  }
  
  @Test
  void shouldFail_whenInvalidDateOrder() {
    var request = GrpcTestUtil.buildGetTicketCategoryScoresRequest("2025-07-04T00:00:00", "2025-06-04T00:00:00");
    
    assertThatThrownBy(() -> grpcStub.getTicketCategoryScores(request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageContaining("INVALID_ARGUMENT: End date must not be before startDate date");
  }
  
  @Test
  void testGetTicketScores() {
    var startDate = "2025-07-01T00:00:00";
    var endDate = "2025-07-02T00:00:00";
    
    var request = GrpcTestUtil.buildGetTicketCategoryScoresRequest(startDate, endDate);
    
    GetTicketCategoryScoresResponse response = grpcStub.getTicketCategoryScores(request);
    
    assertThat(response.getTicketScoresList()).isNotEmpty();
    for (TicketCategoryScore row : response.getTicketScoresList()) {
      assertThat(row.getTicketId()).isGreaterThan(0);
      assertThat(row.getCategoryScoresMap()).isNotEmpty();
    }
  }
  
  @ParameterizedTest
  @MethodSource("getTicketCategoryScoresTestData")
  void canGetTicketCategoryScores(
      String startDate,
      String endDate,
      List<Expected> expectedList
  ) {
    var request = GrpcTestUtil.buildGetTicketCategoryScoresRequest(startDate, endDate);
    var response = grpcStub.getTicketCategoryScores(request);
    
    assertThat(response.getTicketScoresList()).isNotEmpty();
    
    List<Integer> expectedTicketIds = expectedList.stream().map(it -> it.ticketId).toList();
    List<Integer> actualTicketIds = response.getTicketScoresList()
        .stream()
        .map(TicketCategoryScore::getTicketId)
        .toList();
    
    assertThat(actualTicketIds).containsExactlyInAnyOrderElementsOf(expectedTicketIds);
    
    
    for (var ticketId : expectedTicketIds) {
      var ticketScore = response.getTicketScoresList()
          .stream()
          .filter(t -> t.getTicketId() == ticketId)
          .findFirst()
          .orElseThrow(() -> new AssertionError("TicketId " + ticketId + " not found in response"));
      
      var actualCategoryScores = ticketScore.getCategoryScoresMap();
      var expectedCategoryScores = expectedList.stream()
          .filter(it -> Objects.equals(it.ticketId, ticketId))
          .findFirst()
          .orElseThrow(() -> new AssertionError("TicketId " + ticketId + " not found in response"))
          .categoryScores;
      
      assertThat(actualCategoryScores).containsExactlyInAnyOrderEntriesOf(expectedCategoryScores);
    }
  }
  
  static Stream<Arguments> getTicketCategoryScoresTestData() {
    return Stream.of(
        // ticket 1, category 1 (Spelling): rating 4, weight 1, score = 4*1/1*5*100 = 80.00 approx
        arguments("2025-07-01T00:00:00", "2025-07-02T23:59:59",
            List.of(
                Expected.of(1, Map.of(
                    1, 80d,
                    2, 60d
                )),
                Expected.of(2, Map.of(
                    1, 100d,
                    3, 40d
                ))
            )
        )

//        // ticket 1, category 2 (Grammar): rating 3, weight 0.7, score = 3*0.7/0.7*5*100 = 60.00 approx
//        arguments("2025-07-01T00:00:00", "2025-07-01T23:59:59", 1, 2, BigDecimal.valueOf(60.00)),
//
//        // ticket 2, category 1 (Spelling): rating 5, weight 1, score = 5*1/1*5*100 = 100.00
//        arguments("2025-07-02T00:00:00", "2025-07-02T23:59:59", 2, 1, BigDecimal.valueOf(100.00)),
//
//        // ticket 2, category 3 (GDPR): rating 2, weight 1.2, score = 2*1.2/1.2*5*100 = 40.00
//        arguments("2025-07-02T00:00:00", "2025-07-02T23:59:59", 2, 3, BigDecimal.valueOf(40.00)),
//
//        // ticket 3, category 4 (Randomness): rating 4, weight 1, score = 4*1/1*5*100 = 80.00
//        arguments("2025-07-03T00:00:00", "2025-07-03T23:59:59", 3, 4, BigDecimal.valueOf(80.00)),
//
//        // ticket 4, category 2 (Grammar): rating 1, weight 0.7, score = 1*0.7/0.7*5*100 = 20.00
//        arguments("2025-07-04T00:00:00", "2025-07-04T23:59:59", 4, 2, BigDecimal.valueOf(20.00))
    );
  }
  
  private record Expected(
      Integer ticketId,
      Map<Integer, Double> categoryScores
  ) {
    public static Expected of(Integer ticketId, Map<Integer, Double> categoryScores) {
      return new Expected(ticketId, categoryScores);
    }
  }
}
