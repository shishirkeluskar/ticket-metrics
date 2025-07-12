package com.shishir.ticketmetrics.grpc;

import com.shishir.ticketmetrics.service.TicketScoringService;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class TicketMetricsGrpcService extends TicketMetricsServiceGrpc.TicketMetricsServiceImplBase {
  private final TicketScoringService ticketScoringService;
  
  public TicketMetricsGrpcService(TicketScoringService ticketScoringService) {
    this.ticketScoringService = ticketScoringService;
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
}
