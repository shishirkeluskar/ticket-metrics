package com.shishir.ticketmetrics.testsupport.utl;

import com.shishir.ticketmetrics.generated.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcTestUtil {
  public static ManagedChannel buildManagedChannel(String address, int port) {
    return ManagedChannelBuilder.forAddress(address, port)
        .usePlaintext()
        .build();
  }
  
  public static ManagedChannel buildManagedChannel(int port) {
    return buildManagedChannel("localhost", port);
  }
  
  public static TicketMetricsServiceGrpc.TicketMetricsServiceBlockingStub buildServiceStub(ManagedChannel channel) {
    return TicketMetricsServiceGrpc.newBlockingStub(channel);
  }
  
  public static GetTicketScoreRequest buildGetTicketScoreRequest(int value) {
    return GetTicketScoreRequest.newBuilder()
        .setTicketId(value)
        .build();
  }
  
  public static CategoryTimelineRequest buildGetCategoryTimelineScoresRequest(String startDate, String endDate) {
    return CategoryTimelineRequest.newBuilder()
        .setStartDate(startDate)
        .setEndDate(endDate)
        .build();
  }
  
  public static OverallQualityScoreRequest buildOverallQualityScoreRequest(String startDate, String endDate) {
    return OverallQualityScoreRequest.newBuilder()
        .setStartDate(startDate)
        .setEndDate(endDate)
        .build();
  }
  
  public static PeriodScoreComparisonRequest buildComparePeriodScoresRequest(
      String currentStartDate,
      String currentEndDate,
      String previousStartDate,
      String previousEndDate
  ) {
    return PeriodScoreComparisonRequest.newBuilder()
        .setCurrentStartDate(currentStartDate)
        .setCurrentEndDate(currentEndDate)
        .setPreviousStartDate(previousStartDate)
        .setPreviousEndDate(previousEndDate)
        .build();
  }
  
  public static TicketCategoryMatrixRequest buildGetTicketCategoryMatrixRequest(String startDate, String endDate) {
    return TicketCategoryMatrixRequest.newBuilder()
        .setStartDate(startDate)
        .setEndDate(endDate)
        .build();
  }
}
