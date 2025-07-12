package com.shishir.ticketmetrics.util;

import com.shishir.ticketmetrics.model.TimeBucket;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeBucketResolver {
  public static TimeBucket resolve(LocalDateTime start, LocalDateTime end) {
    long days = ChronoUnit.DAYS.between(start, end);
    return (days <= 31) ? TimeBucket.DAILY : TimeBucket.WEEKLY;
  }
}
