package com.shishir.ticketmetrics.grpc.support;

import com.shishir.ticketmetrics.generated.grpc.GetTicketScoreRequest;
import com.shishir.ticketmetrics.generated.grpc.GetTicketScoreResponse;
import com.shishir.ticketmetrics.service.TicketScoreService;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class GrpcRequestHandler {
  private final TicketScoreService ticketScoreService;
  
  public GrpcRequestHandler(TicketScoreService ticketScoreService) {
    this.ticketScoreService = ticketScoreService;
  }
  
  public GetTicketScoreResponse handle(GetTicketScoreRequest request) {
    validateGetTicketScoreRequest(request);
    var score = ticketScoreService.getTicketScore(request.getTicketId());
    return GetTicketScoreResponse.newBuilder()
        .setScore(score.setScale(0, RoundingMode.HALF_EVEN).doubleValue())
        .build();
  }
  
  private void validateGetTicketScoreRequest(GetTicketScoreRequest request) {
    GrpcValidationUtils.validatePositive(request.getTicketId(), "ticket_id");
  }
}
