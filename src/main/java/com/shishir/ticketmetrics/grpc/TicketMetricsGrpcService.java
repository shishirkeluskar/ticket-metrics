package com.shishir.ticketmetrics.grpc;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.grpc.support.GrpcValidationUtils;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.service.OverallScoreService;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import com.shishir.ticketmetrics.service.TicketScoringService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@GrpcService
public class TicketMetricsGrpcService extends TicketMetricsServiceGrpc.TicketMetricsServiceImplBase {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  private final TicketScoringService ticketScoringService;
  private final OverallScoreService overallScoreService;
  private final ScoreAggregationService timelineService;
  
  
  public TicketMetricsGrpcService(TicketScoringService ticketScoreService, OverallScoreService overallScoreService, ScoreAggregationService timelineService) {
    this.ticketScoringService = ticketScoreService;
    this.overallScoreService = overallScoreService;
    this.timelineService = timelineService;
  }
  
  @Override
  public void getTicketScore(GetTicketScoreRequest request, StreamObserver<GetTicketScoreResponse> responseObserver) {
    try {
      validateGetTicketScoreRequest(request);
      double score = ticketScoringService.getTicketScore(request.getTicketId());
      
      var response = GetTicketScoreResponse.newBuilder()
          .setScore(score)
          .build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void getCategoryTimelineScores(CategoryTimelineRequest request, StreamObserver<CategoryTimelineResponse> responseObserver) {
    try {
      validateCategoryTimelineRequest(request);
      
      var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
      var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
      GrpcValidationUtils.validateDateOrder(startDate, endDate);
      
      Map<Integer, CategoryScoreSummary> scoreMap = timelineService.getCategoryScoresOverTime(startDate, endDate);
      
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
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void getTicketCategoryMatrix(TicketCategoryMatrixRequest request, StreamObserver<TicketCategoryMatrixResponse> responseObserver) {
    try {
      validateTicketCategoryMatrixRequest(request);
      
      var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
      var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
      
      GrpcValidationUtils.validateDateOrder(startDate, endDate);
      
      Map<Integer, Map<Integer, BigDecimal>> scoresByTicket = timelineService.getScoresByTicket(startDate, endDate);
      
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
      
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void getOverallQualityScore(OverallQualityScoreRequest request, StreamObserver<OverallQualityScoreResponse> responseObserver) {
    try {
      validateOverallQualityScoreRequest(request);
      
      var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
      var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
      
      GrpcValidationUtils.validateDateOrder(startDate, endDate);
      
      //BigDecimal overallScore = timelineService.getOverallScore(startDate, endDate);
      BigDecimal overallScore = overallScoreService.getOverallScore(startDate.toLocalDate(), endDate.toLocalDate());
      
      OverallQualityScoreResponse response = OverallQualityScoreResponse.newBuilder()
          .setScore(overallScore.doubleValue())
          .build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void comparePeriodScores(PeriodScoreComparisonRequest request, StreamObserver<PeriodScoreComparisonResponse> responseObserver) {
    try {
      validatePeriodScoreComparisonRequest(request);
      
      var currentStartDate = GrpcValidationUtils.parseIsoDateTime(request.getCurrentStartDate(), "current_start_date");
      var currentEndDate = GrpcValidationUtils.parseIsoDateTime(request.getCurrentEndDate(), "current_end_date");
      
      GrpcValidationUtils.validateDateOrder(currentStartDate, currentEndDate);
      
      var change = timelineService.calculatePeriodOverPeriodChange(currentStartDate, currentEndDate);
      
      PeriodScoreComparisonResponse response = PeriodScoreComparisonResponse.newBuilder()
          .setCurrentPeriodScore(change.currentScore().doubleValue())
          .setPreviousPeriodScore(change.previousScore().doubleValue())
          .setScoreChange(change.change().doubleValue())
          .build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  private String fromLocalDateTimetoString(LocalDateTime date) {
    return date.format(FORMATTER);
  }
  
  private void validateGetTicketScoreRequest(GetTicketScoreRequest request) {
    GrpcValidationUtils.validatePositive(request.getTicketId(), "ticket_id");
  }
  
  private void validateCategoryTimelineRequest(CategoryTimelineRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
  
  private void validateTicketCategoryMatrixRequest(TicketCategoryMatrixRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
  
  private void validateOverallQualityScoreRequest(OverallQualityScoreRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
  
  private void validatePeriodScoreComparisonRequest(PeriodScoreComparisonRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getCurrentStartDate(), "current_start_date");
    GrpcValidationUtils.validateNotBlank(request.getCurrentEndDate(), "current_end_date");
  }
}
