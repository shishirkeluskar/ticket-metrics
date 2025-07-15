package com.shishir.ticketmetrics.grpc.support;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.service.OverallScoreService;
import com.shishir.ticketmetrics.service.TicketScoreService;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class GrpcRequestHandler {
  private final TicketScoreService ticketScoreService;
  private final OverallScoreService overallScoreService;
  
  public GrpcRequestHandler(TicketScoreService ticketScoreService, OverallScoreService overallScoreService) {
    this.ticketScoreService = ticketScoreService;
    this.overallScoreService = overallScoreService;
  }
  
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
  
  private void validateGetTicketScoreRequest(GetTicketScoreRequest request) {
    GrpcValidationUtils.validatePositive(request.getTicketId(), "ticket_id");
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
