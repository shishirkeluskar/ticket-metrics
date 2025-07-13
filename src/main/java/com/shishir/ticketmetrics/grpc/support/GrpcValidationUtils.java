package com.shishir.ticketmetrics.grpc.support;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class GrpcValidationUtils {
  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  
  /**
   * Validates that a string is not null, empty, or blank.
   *
   * @param value     the value to check
   * @param fieldName the name of the field (for error messages)
   */
  public static void validateNotBlank(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw statusInvalid("%s must not be blank", fieldName);
    }
  }
  
  /**
   * Validates that a number is positive (> 0).
   *
   * @param value     the value to check
   * @param fieldName the name of the field
   */
  public static void validatePositive(int value, String fieldName) {
    if (value <= 0) {
      throw statusInvalid("%s must be a positive number", fieldName);
    }
  }
  
  /**
   * Parses an ISO 8601 string into a {@link LocalDateTime}, or throws a gRPC error if invalid.
   *
   * @param value     the string to parse
   * @param fieldName the name of the field
   * @return parsed LocalDateTime
   */
  public static LocalDateTime parseIsoDateTime(String value, String fieldName) {
    try {
      return LocalDateTime.parse(value, ISO_FORMATTER);
    } catch (DateTimeParseException e) {
      throw statusInvalid("%s must be in ISO date-time format but was %s", fieldName, value);
    }
  }
  
  /**
   * Validates that startDate date is before or equal to endDate date.
   *
   * @param startDate startDate time
   * @param endDate   endDate time
   */
  public static void validateDateOrder(LocalDateTime startDate, LocalDateTime endDate) {
    if (endDate.isBefore(startDate)) {
      throw statusInvalid("End date must not be before startDate date");
    }
  }
  
  public static LocalDateTime toLocalDateTime(Timestamp ts) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()), ZoneOffset.UTC);
  }
  
  public static Timestamp toProtoTimestamp(LocalDateTime dateTime) {
    Instant instant = dateTime.toInstant(ZoneOffset.UTC);
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
  
  /**
   * Utility to throw INVALID_ARGUMENT gRPC exception.
   *
   * @param message error message format string
   * @param args    format args
   * @return StatusRuntimeException
   */
  public static StatusRuntimeException statusInvalid(String message, Object... args) {
    return Status.INVALID_ARGUMENT
        .withDescription(String.format(message, args))
        .asRuntimeException();
  }
}
