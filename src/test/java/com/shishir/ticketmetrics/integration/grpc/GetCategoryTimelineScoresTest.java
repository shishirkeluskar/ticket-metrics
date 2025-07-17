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

import java.util.List;
import java.util.Objects;
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
  void canGetCategoryTimelineScores(
      String startDate,
      String endDate,
      List<Expected> expectedScores) {
    var request = GrpcTestUtil.buildGetCategoryTimelineScoresRequest(startDate, endDate);
    
    var response = grpcStub.getCategoryTimelineScores(request);
    
    
    // Transform response for comparison with expected output
    var actualScores = response.getScoresList().stream()
        .map(actualScore -> Expected.of(
            actualScore.getCategoryId(),
            actualScore.getTotalRatings(),
            actualScore.getAverageScore(),
            actualScore.getTimelineList().stream()
                .map(actualTimeline -> ExpectedTimeline.of(actualTimeline.getDate(), actualTimeline.getScore()))
                .toList()
        ))
        .toList();
    
    var categoryIds = actualScores.stream().map(it -> it.categoryId).toList();
    
    for (var categoryId : categoryIds) {
      var actualScore = actualScores.stream().filter(it -> Objects.equals(it.categoryId, categoryId)).findFirst().get();
      var expectedScore = expectedScores.stream().filter(it -> Objects.equals(it.categoryId, categoryId)).findFirst().get();
      
      assertThat(actualScore)
          .satisfies(it -> {
            assertThat(it.categoryId).isEqualTo(expectedScore.categoryId);
            assertThat(it.averageScore).isEqualTo(expectedScore.averageScore);
            assertThat(it.totalRatings).isEqualTo(expectedScore.totalRatings);
            
            // Check timelines
            var actualTimeline = it.timeline;
            var expectedTimeline = expectedScore.timeline;
            
            assertThat(actualTimeline)
                .as("Category id = " + it.categoryId)
                .containsExactlyInAnyOrderElementsOf(expectedTimeline);
          });
    }
  }
  
  static Stream<Arguments> categoryTimelineCasesTestData() {
    return Stream.of(
        arguments(
            "2025-07-01T00:00:00", "2025-07-03T23:59:59",
            List.of(
                Expected.of(1, 2, 90d,
                    List.of(
                        ExpectedTimeline.of("2025-07-01", 80.0),
                        ExpectedTimeline.of("2025-07-03", 100.0)
                    )
                ),
                Expected.of(2, 1, 60d,
                    List.of(
                        ExpectedTimeline.of("2025-07-02", 60.0)
                    )
                )
            )
        ),// daily
        arguments(
            "2025-07-01T00:00:00", "2025-10-09T00:00:00",
            List.of(
                Expected.of(1, 7, 92d,
                    List.of(
                        ExpectedTimeline.of("2025-06-30", 90.0),
                        ExpectedTimeline.of("2025-07-28", 90.0),
                        ExpectedTimeline.of("2025-08-11", 92.0),
                        ExpectedTimeline.of("2025-09-08", 90.0),
                        ExpectedTimeline.of("2025-07-21", 92.0)
                    )
                ),
                Expected.of(2, 7, 66d,
                    List.of(
                        ExpectedTimeline.of("2025-07-14", 80.0),
                        ExpectedTimeline.of("2025-06-30", 70.0),
                        ExpectedTimeline.of("2025-07-28", 67.0),
                        ExpectedTimeline.of("2025-08-11", 65.0),
                        ExpectedTimeline.of("2025-09-08", 64.0),
                        ExpectedTimeline.of("2025-07-21", 63.0),
                        ExpectedTimeline.of("2025-08-04", 66.0)
                    )
                ),
                Expected.of(3, 2, 40d,
                    List.of(
                        ExpectedTimeline.of("2025-07-07", 40.0),
                        ExpectedTimeline.of("2025-08-04", 40.0)
                    )
                )
            )
        )
    );  // weekly
  }
  
  record Expected(
      Integer categoryId,
      Integer totalRatings,
      Double averageScore,
      List<ExpectedTimeline> timeline
  ) {
    public static Expected of(Integer categoryId, Integer totalRatings, Double averageScore, List<ExpectedTimeline> timeline) {
      return new Expected(categoryId, totalRatings, averageScore, timeline);
    }
  }
  
  record ExpectedTimeline(
      String date,
      Double score
  ) {
    public static ExpectedTimeline of(String date, Double score) {
      return new ExpectedTimeline(date, score);
    }
  }
}
