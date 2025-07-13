package com.shishir.ticketmetrics.testsupport.utl;

import com.shishir.ticketmetrics.generated.grpc.TicketMetricsServiceGrpc;
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
}
