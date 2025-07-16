package com.shishir.ticketmetrics.grpc;

import com.shishir.ticketmetrics.generated.grpc.*;
import com.shishir.ticketmetrics.grpc.support.GrpcRequestHandler;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class TicketMetricsGrpcService extends TicketMetricsServiceGrpc.TicketMetricsServiceImplBase {
  private static final Logger LOG = LoggerFactory.getLogger(TicketMetricsGrpcService.class);
  private final GrpcRequestHandler handler;
  
  
  public TicketMetricsGrpcService(GrpcRequestHandler handler) {
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
  public void getTicketCategoryScores(GetTicketCategoryScoresRequest request, StreamObserver<GetTicketCategoryScoresResponse> responseObserver) {
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
}
