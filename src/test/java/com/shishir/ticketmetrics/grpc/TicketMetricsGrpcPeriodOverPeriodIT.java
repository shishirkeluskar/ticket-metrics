package com.shishir.ticketmetrics.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.LocalGrpcPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TicketMetricsGrpcPeriodOverPeriodIT {
  
  @LocalGrpcPort
  private int port;
  private ManagedChannel channel;
  private TicketMetricsServiceGrpc.TicketMetricsServiceBlockingStub stub;
  
  @BeforeEach
  void setup() {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext()
        .build();
    stub = TicketMetricsServiceGrpc.newBlockingStub(channel);
  }
  
  
  @Test
  void testGetPeriodOverPeriodScoreChange() {
    // 👇 These dates must match existing DB values if using real db
    String currentStart = "2020-01-01T00:00:00";
    String currentEnd = "2020-01-31T00:00:00";
    
    PeriodOverPeriodRequest request = PeriodOverPeriodRequest.newBuilder()
        .setCurrentStart(currentStart)
        .setCurrentEnd(currentEnd)
        .build();
    
    PeriodOverPeriodResponse response = stub.getPeriodOverPeriodScoreChange(request);
    
    // 👇 Basic validations (replace with known values if DB has data)
    assertThat(response.getCurrentPeriodScore()).isGreaterThanOrEqualTo(0);
    assertThat(response.getPreviousPeriodScore()).isGreaterThanOrEqualTo(0);
  }
}
