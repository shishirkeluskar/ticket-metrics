package com.shishir.ticketmetrics.grpc.support;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.service.OverallScoreService;
import com.shishir.ticketmetrics.service.ScoreAggregationService;
import com.shishir.ticketmetrics.service.TicketScoreService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class GrpcRequestHandler {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  
  private final TicketScoreService ticketScoreService;
  private final OverallScoreService overallScoreService;
  private final ScoreAggregationService timelineService;
  
  public GrpcRequestHandler(TicketScoreService ticketScoreService, OverallScoreService overallScoreService, ScoreAggregationService timelineService) {
    this.ticketScoreService = ticketScoreService;
    this.overallScoreService = overallScoreService;
    this.timelineService = timelineService;
  }
  
  // --- Request & Response handers ---
  
  public GetTicketScoreResponse handle(GetTicketScoreRequest request) {
    // Validate
    validateGetTicketScoreRequest(request);
    
    // Process
    var score = ticketScoreService.getTicketScore(request.getTicketId());
    
    // Build response
    return GetTicketScoreResponse.newBuilder()
        .setScore(score.setScale(0, RoundingMode.HALF_EVEN).doubleValue())
        .build();
  }
  
  public CategoryTimelineResponse handle(CategoryTimelineRequest request) {
    // Validate
    validateCategoryTimelineRequest(request);
    var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
    var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
    GrpcValidationUtils.validateDateOrder(startDate, endDate);
    
    // Process
    Map<Integer, CategoryScoreSummary> scoreMap = timelineService.getCategoryScoresOverTime(startDate, endDate);
    
    // Build response
    var responseBuilder = CategoryTimelineResponse.newBuilder();
    
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
    return responseBuilder.build();
  }
  
  public GetTicketCategoryScoresResponse handle(GetTicketCategoryScoresRequest request) {
    // Validate
    validateGetTicketCategoryScoresRequest(request);
    var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
    var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
    GrpcValidationUtils.validateDateOrder(startDate, endDate);
    
    // Process
    Map<Integer, Map<Integer, BigDecimal>> scoresByTicket = timelineService.getScoresByTicket(
        startDate.toLocalDate(),
        endDate.toLocalDate()
    );
    
    // Build response
    GetTicketCategoryScoresResponse.Builder responseBuilder = GetTicketCategoryScoresResponse.newBuilder();
    for (Map.Entry<Integer, Map<Integer, BigDecimal>> ticketEntry : scoresByTicket.entrySet()) {
      TicketCategoryScore.Builder ticketScoreRowBuilder = TicketCategoryScore.newBuilder();
      ticketScoreRowBuilder.setTicketId(ticketEntry.getKey());
      
      for (Map.Entry<Integer, BigDecimal> categoryEntry : ticketEntry.getValue().entrySet()) {
        ticketScoreRowBuilder.putCategoryScores(categoryEntry.getKey(), categoryEntry.getValue().doubleValue());
      }
      
      responseBuilder.addTicketScores(ticketScoreRowBuilder);
    }
    return responseBuilder.build();
  }
  
  public OverallQualityScoreResponse handle(OverallQualityScoreRequest request) {
    // Validate
    validateOverallQualityScoreRequest(request);
    var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
    var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
    GrpcValidationUtils.validateDateOrder(startDate, endDate);
    
    // Process
    var overallScore = overallScoreService.getOverallScore(startDate.toLocalDate(), endDate.toLocalDate());
    
    // Build response
    return OverallQualityScoreResponse.newBuilder()
        .setScore(overallScore.setScale(0, RoundingMode.HALF_EVEN).doubleValue())
        .build();
  }
  
  public PeriodScoreComparisonResponse handle(PeriodScoreComparisonRequest request) {
    // Validate
    validatePeriodScoreComparisonRequest(request);
    var currentStartDate = GrpcValidationUtils.parseIsoDateTime(request.getCurrentStartDate(), "current_start_date");
    var currentEndDate = GrpcValidationUtils.parseIsoDateTime(request.getCurrentEndDate(), "current_end_date");
    var previousStartDate = GrpcValidationUtils.parseIsoDateTime(request.getPreviousStartDate(), "previous_start_date");
    var previousEndDate = GrpcValidationUtils.parseIsoDateTime(request.getPreviousEndDate(), "previous_end_date");
    GrpcValidationUtils.validateDateOrder(currentStartDate, currentEndDate);
    GrpcValidationUtils.validateDateOrder(previousStartDate, previousEndDate);
    
    // Process
    var currentScore = overallScoreService.getOverallScore(currentStartDate.toLocalDate(), currentEndDate.toLocalDate());
    var previousScore = overallScoreService.getOverallScore(previousStartDate.toLocalDate(), previousEndDate.toLocalDate());
    var change = currentScore.subtract(previousScore).setScale(2, RoundingMode.HALF_UP);
    
    // Build response
    return PeriodScoreComparisonResponse.newBuilder()
        .setCurrentPeriodScore(currentScore.setScale(0, RoundingMode.HALF_EVEN).doubleValue())
        .setPreviousPeriodScore(previousScore.setScale(0, RoundingMode.HALF_EVEN).doubleValue())
        .setScoreChange(change.setScale(0, RoundingMode.HALF_EVEN).doubleValue())
        .build();
  }
  
  // --- Helpers ---
  
  private void validateGetTicketScoreRequest(GetTicketScoreRequest request) {
    GrpcValidationUtils.validatePositive(request.getTicketId(), "ticket_id");
  }
  
  private String fromLocalDateTimetoString(LocalDateTime date) {
    return date.format(FORMATTER);
  }
  
  private void validateCategoryTimelineRequest(CategoryTimelineRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
  
  private void validateGetTicketCategoryScoresRequest(GetTicketCategoryScoresRequest request) {
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
    GrpcValidationUtils.validateNotBlank(request.getPreviousStartDate(), "previous_start_date");
    GrpcValidationUtils.validateNotBlank(request.getPreviousEndDate(), "previous_end_date");
  }
}
