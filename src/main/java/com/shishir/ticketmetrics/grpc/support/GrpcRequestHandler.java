package com.shishir.ticketmetrics.grpc.support;

import com.shishir.ticketmetrics.generated.grpc.GetTicketScoreRequest;
import com.shishir.ticketmetrics.generated.grpc.GetTicketScoreResponse;
import com.shishir.ticketmetrics.generated.grpc.OverallQualityScoreRequest;
import com.shishir.ticketmetrics.generated.grpc.OverallQualityScoreResponse;
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
  
  private void validateGetTicketScoreRequest(GetTicketScoreRequest request) {
    GrpcValidationUtils.validatePositive(request.getTicketId(), "ticket_id");
  }
  
  private void validateOverallQualityScoreRequest(OverallQualityScoreRequest request) {
    GrpcValidationUtils.validateNotBlank(request.getStartDate(), "start_date");
    GrpcValidationUtils.validateNotBlank(request.getEndDate(), "end_date");
  }
}
