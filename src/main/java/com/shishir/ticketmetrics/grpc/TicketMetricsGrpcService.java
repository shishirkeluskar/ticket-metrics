package com.shishir.ticketmetrics.grpc;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.grpc.support.GrpcRequestHandler;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

import java.util.function.Function;

@GrpcService
public class TicketMetricsGrpcService extends TicketMetricsServiceGrpc.TicketMetricsServiceImplBase {
  private static final Logger LOG = LoggerFactory.getLogger(TicketMetricsGrpcService.class);
  private final GrpcRequestHandler handler;
  
  public TicketMetricsGrpcService(GrpcRequestHandler handler) {
    this.handler = handler;
  }
  
  @Override
  public void getTicketScore(GetTicketScoreRequest request, StreamObserver<GetTicketScoreResponse> responseObserver) {
    handleGrpcCall(request, handler::handle, responseObserver);
  }
  
  @Override
  public void getCategoryTimelineScores(CategoryTimelineRequest request, StreamObserver<CategoryTimelineResponse> responseObserver) {
    handleGrpcCall(request, handler::handle2, responseObserver);
  }
  
  @Override
  public void getTicketCategoryMatrix(TicketCategoryMatrixRequest request, StreamObserver<TicketCategoryMatrixResponse> responseObserver) {
    handleGrpcCall(request, handler::handle, responseObserver);
  }
  
  @Override
  public void getOverallQualityScore(OverallQualityScoreRequest request, StreamObserver<OverallQualityScoreResponse> responseObserver) {
    handleGrpcCall(request, handler::handle, responseObserver);
  }
  
  @Override
  public void comparePeriodScores(PeriodScoreComparisonRequest request, StreamObserver<PeriodScoreComparisonResponse> responseObserver) {
    handleGrpcCall(request, handler::handle, responseObserver);
  }
  
  private <REQ, RESP> void handleGrpcCall(REQ request, Function<REQ, RESP> handlerFn, StreamObserver<RESP> responseObserver) {
    try {
      RESP response = handlerFn.apply(request);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (StatusRuntimeException e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOG.error("Error encountered.", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
    }
  }
}
