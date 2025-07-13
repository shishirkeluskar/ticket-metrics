package com.shishir.ticketmetrics.grpc;

import com.google.protobuf.Timestamp;
import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import com.shishir.ticketmetrics.service.TicketScoringService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
  public void getCategoryTimelineScores(CategoryTimelineRequest request, StreamObserver<CategoryTimelineResponse> responseObserver) {
    LocalDateTime start = toLocalDateTime(request.getStartDate());
    LocalDateTime end = toLocalDateTime(request.getEndDate());
    
    Map<Integer, CategoryScoreSummary> scoreMap = scoreAggregationService.getCategoryScores(start, end);
    
    CategoryTimelineResponse.Builder responseBuilder = CategoryTimelineResponse.newBuilder();
    
    for (Map.Entry<Integer, CategoryScoreSummary> entry : scoreMap.entrySet()) {
      int categoryId = entry.getKey();
      CategoryScoreSummary summary = entry.getValue();
      
      CategoryAggregateScore.Builder categoryScoreBuilder = CategoryAggregateScore.newBuilder()
          .setCategoryId(categoryId)
          .setTotalRatings(summary.getTotalRatings())
          .setAverageScore(summary.getFinalAverageScore().doubleValue());
      
      // Aggregate scores based on rating creation date
      summary.getDateScores().forEach((dateTime, score) -> {
        categoryScoreBuilder.addTimeline(
            CategoryScoreTimelineEntry.newBuilder()
                .setTimestamp(fromLocalDateTimetoString(dateTime))
                .setScore(score.doubleValue())
                .build()
        );
      });
      
      responseBuilder.addScores(categoryScoreBuilder.build());
    }
    
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }
  
  @Override
  public void getTicketCategoryMatrix(TicketCategoryMatrixRequest request, StreamObserver<TicketCategoryMatrixResponse> responseObserver) {
    try {
      LocalDateTime start = LocalDateTime.parse(request.getStartDate());
      LocalDateTime end = LocalDateTime.parse(request.getEndDate());
      
      Map<Integer, Map<Integer, BigDecimal>> scoresByTicket = scoreAggregationService.getScoresByTicket(start, end);
      
      TicketCategoryMatrixResponse.Builder responseBuilder = TicketCategoryMatrixResponse.newBuilder();
      
      for (Map.Entry<Integer, Map<Integer, BigDecimal>> ticketEntry : scoresByTicket.entrySet()) {
        TicketCategoryScoreRow.Builder ticketScoreRowBuilder = TicketCategoryScoreRow.newBuilder();
        ticketScoreRowBuilder.setTicketId(ticketEntry.getKey());
        
        for (Map.Entry<Integer, BigDecimal> categoryEntry : ticketEntry.getValue().entrySet()) {
          ticketScoreRowBuilder.putCategoryScores(categoryEntry.getKey(), categoryEntry.getValue().doubleValue());
        }
        
        responseBuilder.addTicketScores(ticketScoreRowBuilder);
      }
      
      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();
      
    } catch (DateTimeParseException ex) {
      responseObserver.onError(new IllegalArgumentException("Invalid date format. Use ISO-8601 format."));
    } catch (Exception ex) {
      responseObserver.onError(ex);
    }
  }
  
  @Override
  public void getOverallQualityScore(OverallScoreRequest request, StreamObserver<OverallScoreResponse> responseObserver) {
    try {
      LocalDateTime start = LocalDateTime.parse(request.getStart());
      LocalDateTime end = LocalDateTime.parse(request.getEnd());
      
      BigDecimal overallScore = scoreAggregationService.getOverallScore(start, end);
      
      OverallScoreResponse response = OverallScoreResponse.newBuilder()
          .setOverallScore(overallScore.doubleValue())
          .build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      
    } catch (DateTimeParseException ex) {
      responseObserver.onError(new IllegalArgumentException("Invalid date format. Use ISO-8601 format."));
    } catch (Exception ex) {
      responseObserver.onError(ex);
    }
  }
  
  @Override
  public void comparePeriodScores(PeriodOverPeriodRequest request,
                                  StreamObserver<PeriodOverPeriodResponse> responseObserver) {
    try {
      LocalDateTime currentStart = LocalDateTime.parse(request.getCurrentStart());
      LocalDateTime currentEnd = LocalDateTime.parse(request.getCurrentEnd());
      
      var change = scoreAggregationService.calculatePeriodOverPeriodChange(currentStart, currentEnd);
      
      PeriodOverPeriodResponse response = PeriodOverPeriodResponse.newBuilder()
          .setCurrentPeriodScore(change.currentScore().doubleValue())
          .setPreviousPeriodScore(change.previousScore().doubleValue())
          .setScoreChange(change.change().doubleValue())
          .build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      
    } catch (DateTimeParseException ex) {
      responseObserver.onError(new IllegalArgumentException("Invalid date format. Use ISO-8601 format."));
    } catch (Exception ex) {
      responseObserver.onError(ex);
    }
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
