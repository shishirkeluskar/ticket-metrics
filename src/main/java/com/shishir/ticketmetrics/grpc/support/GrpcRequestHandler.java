package com.shishir.ticketmetrics.grpc.support;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.model.CategoryScoreSummary;
import com.shishir.ticketmetrics.service.*;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class GrpcRequestHandler {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  
  private final TicketScoreService ticketScoreService;
  private final OverallScoreService overallScoreService;
  private final GetCategoryTimelineScoreService getCategoryTimelineScoreService;
  private final GetCategoryTimelineScoreService2 getCategoryTimelineScoreService2;
  private final TicketCategoryMatrixService ticketCategoryMatrixService;
  
  public GrpcRequestHandler(TicketScoreService ticketScoreService, OverallScoreService overallScoreService, GetCategoryTimelineScoreService getCategoryTimelineScoreService, GetCategoryTimelineScoreService2 getCategoryTimelineScoreService2, TicketCategoryMatrixService ticketCategoryMatrixService) {
    this.ticketScoreService = ticketScoreService;
    this.overallScoreService = overallScoreService;
    this.getCategoryTimelineScoreService = getCategoryTimelineScoreService;
    this.getCategoryTimelineScoreService2 = getCategoryTimelineScoreService2;
    this.ticketCategoryMatrixService = ticketCategoryMatrixService;
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
    Map<Integer, CategoryScoreSummary> scoreMap = getCategoryTimelineScoreService.getCategoryScoresOverTime(startDate, endDate);
    
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
                .setDate(fromLocalDateTimetoString(dateTime))
                .setScore(score.doubleValue())
                .build()
        );
      });
      
      responseBuilder.addScores(categoryScoreBuilder.build());
    }
    return responseBuilder.build();
  }
  
  public CategoryTimelineResponse handle2(CategoryTimelineRequest request) {
    // Validate
    validateCategoryTimelineRequest(request);
    var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
    var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
    GrpcValidationUtils.validateDateOrder(startDate, endDate);
    
    // Process
    var scoresSummary = getCategoryTimelineScoreService2.getCategoryTimelineScores(startDate.toLocalDate(), endDate.toLocalDate());
    
    // Build response
    var responseBuilder = CategoryTimelineResponse.newBuilder();
    scoresSummary.forEach(aScoreSummary -> {
      var categoryAggregateScore = CategoryAggregateScore.newBuilder();
      categoryAggregateScore.setCategoryId(aScoreSummary.categoryId());
      categoryAggregateScore.setTotalRatings(aScoreSummary.ratingsCount().intValue());
      categoryAggregateScore.setAverageScore(aScoreSummary.averageScore().setScale(0, RoundingMode.HALF_EVEN).doubleValue());
      aScoreSummary.timeline().forEach(timeline ->
          categoryAggregateScore.addTimeline(CategoryScoreTimelineEntry.newBuilder()
              .setDate(timeline.date().toString())
              .setScore(timeline.score().setScale(0, RoundingMode.HALF_EVEN).doubleValue())
              .build()
          )
      );
      
      responseBuilder.addScores(categoryAggregateScore.build());
    });
    return responseBuilder.build();
  }
  
  public TicketCategoryMatrixResponse handle(TicketCategoryMatrixRequest request) {
    // Validate
    validateGetTicketCategoryScoresRequest(request);
    var startDate = GrpcValidationUtils.parseIsoDateTime(request.getStartDate(), "start_date");
    var endDate = GrpcValidationUtils.parseIsoDateTime(request.getEndDate(), "end_date");
    GrpcValidationUtils.validateDateOrder(startDate, endDate);
    
    // Process
    var scoreMatrix = ticketCategoryMatrixService.getTicketCategoryScores(startDate.toLocalDate(), endDate.toLocalDate());
    
    // Build response
    var responseBuilder = TicketCategoryMatrixResponse.newBuilder();
    
    scoreMatrix.forEach(row -> {
      var ticketScoreBuilder = TicketCategoryScore.newBuilder();
      ticketScoreBuilder.setTicketId(row.ticketId());
      row.categoryScoreByTickets().forEach(categoryScore -> {
        double score = categoryScore.score().setScale(0, RoundingMode.HALF_EVEN).doubleValue();
        ticketScoreBuilder.putCategoryScores(categoryScore.categoryId(), score);
      });
      responseBuilder.addTicketScores(ticketScoreBuilder.build());
    });
    
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
  
  private void validateGetTicketCategoryScoresRequest(TicketCategoryMatrixRequest request) {
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
