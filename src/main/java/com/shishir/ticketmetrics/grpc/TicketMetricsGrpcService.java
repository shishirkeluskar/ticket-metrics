package com.shishir.ticketmetrics.grpc;

import com.google.protobuf.Timestamp;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import com.shishir.ticketmetrics.service.TicketScoringService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@GrpcService
public class TicketMetricsGrpcService extends TicketMetricsServiceGrpc.TicketMetricsServiceImplBase {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  private final TicketScoringService ticketScoringService;
  private final ScoreAggregationService scoreAggregationService;
  
  
  public TicketMetricsGrpcService(TicketScoringService ticketScoringService, ScoreAggregationService scoreAggregationService) {
    this.ticketScoringService = ticketScoringService;
    this.scoreAggregationService = scoreAggregationService;
  }
  
  @Override
  public void getTicketScore(GetTicketScoreRequest request, StreamObserver<GetTicketScoreResponse> responseObserver) {
    int ticketId = request.getTicketId();
    double score = ticketScoringService.computeScore(ticketId);
    
    var response = GetTicketScoreResponse.newBuilder()
        .setScore(score)
        .build();
    
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
  
  @Override
  public void getCategoryScoreOverTime(CategoryScoreRequest request, StreamObserver<CategoryScoreResponse> responseObserver) {
    LocalDateTime start = toLocalDateTime(request.getStartDate());
    LocalDateTime end = toLocalDateTime(request.getEndDate());
    
    Map<Integer, CategoryScoreSummary> scoreMap = scoreAggregationService.getCategoryScores(start, end);
    
    CategoryScoreResponse.Builder responseBuilder = CategoryScoreResponse.newBuilder();
    
    for (Map.Entry<Integer, CategoryScoreSummary> entry : scoreMap.entrySet()) {
      int categoryId = entry.getKey();
      CategoryScoreSummary summary = entry.getValue();
      
      CategoryScore.Builder categoryScoreBuilder = CategoryScore.newBuilder()
          .setCategoryId(categoryId)
          .setTotalRatings(summary.getTotalRatings())
          .setAverageScore(summary.getFinalAverageScore().doubleValue());
      
      summary.getDateScores().forEach((dateTime, score) -> {
        categoryScoreBuilder.addTimeline(
            ScoreByDate.newBuilder()
                .setDate(fromLocalDateTimetoString(dateTime))
                .setScore(score.doubleValue())
                .build()
        );
      });
      
      responseBuilder.addScores(categoryScoreBuilder.build());
    }
    
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }
  
  private LocalDateTime toLocalDateTime(Timestamp ts) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()), ZoneOffset.UTC);
  }
  
  private Timestamp toProtoTimestamp(LocalDateTime dateTime) {
    Instant instant = dateTime.toInstant(ZoneOffset.UTC);
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
  
  private LocalDateTime toLocalDateTime(String date) {
    return LocalDateTime.parse(date, FORMATTER);
  }
  
  private String fromLocalDateTimetoString(LocalDateTime date) {
    return date.format(FORMATTER);
  }
}
