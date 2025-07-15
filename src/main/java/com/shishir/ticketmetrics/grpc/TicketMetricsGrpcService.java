package com.shishir.ticketmetrics.grpc;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.grpc.support.GrpcRequestHandler;
import com.shishir.ticketmetrics.grpc.support.GrpcValidationUtils;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@GrpcService
public class TicketMetricsGrpcService extends TicketMetricsServiceGrpc.TicketMetricsServiceImplBase {
  private static final Logger LOG = LoggerFactory.getLogger(TicketMetricsGrpcService.class);
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  private final ScoreAggregationService timelineService;
  private final GrpcRequestHandler handler;
  
  
  public TicketMetricsGrpcService(ScoreAggregationService timelineService, GrpcRequestHandler handler) {
    this.timelineService = timelineService;
    this.handler = handler;
  }
  
  @Override
  public void getTicketScore(GetTicketScoreRequest request, StreamObserver<GetTicketScoreResponse> responseObserver) {
    try {
      responseObserver.onNext(handler.handle(request));
      responseObserver.onCompleted();
    } catch (StatusRuntimeException e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOG.error("Error encountered.", e);
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
      LOG.error("Error encountered.", e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void getTicketCategoryScores(GetTicketCategoryScoresRequest request, StreamObserver<GetTicketCategoryScoresResponse> responseObserver) {
    try {
      validateTicketCategoryMatrixRequest(request);
      
      var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
      var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
      
      GrpcValidationUtils.validateDateOrder(startDate, endDate);
      
      Map<Integer, Map<Integer, BigDecimal>> scoresByTicket = timelineService.getScoresByTicket(
          startDate.toLocalDate(),
          endDate.toLocalDate()
      );
      
      GetTicketCategoryScoresResponse.Builder responseBuilder = GetTicketCategoryScoresResponse.newBuilder();
      
      for (Map.Entry<Integer, Map<Integer, BigDecimal>> ticketEntry : scoresByTicket.entrySet()) {
        TicketCategoryScore.Builder ticketScoreRowBuilder = TicketCategoryScore.newBuilder();
        ticketScoreRowBuilder.setTicketId(ticketEntry.getKey());
        
        for (Map.Entry<Integer, BigDecimal> categoryEntry : ticketEntry.getValue().entrySet()) {
          ticketScoreRowBuilder.putCategoryScores(categoryEntry.getKey(), categoryEntry.getValue().doubleValue());
        }
        
        responseBuilder.addTicketScores(ticketScoreRowBuilder);
      }
      
      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();
      
    } catch (StatusRuntimeException e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void getOverallQualityScore(OverallQualityScoreRequest request, StreamObserver<OverallQualityScoreResponse> responseObserver) {
    try {
      responseObserver.onNext(handler.handle(request));
      responseObserver.onCompleted();
    } catch (StatusRuntimeException e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  @Override
  public void comparePeriodScores(PeriodScoreComparisonRequest request, StreamObserver<PeriodScoreComparisonResponse> responseObserver) {
    try {
      responseObserver.onNext(handler.handle(request));
      responseObserver.onCompleted();
    } catch (StatusRuntimeException e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
  
  private String fromLocalDateTimetoString(LocalDateTime date) {
    return date.format(FORMATTER);
  }
  
  private void validateCategoryTimelineRequest(CategoryTimelineRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
  
  private void validateTicketCategoryMatrixRequest(GetTicketCategoryScoresRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
}
