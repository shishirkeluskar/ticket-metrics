package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Rating(
    Integer id,
    BigDecimal rating,
    Integer ticketId,
    Integer ratingCategoryId,
    Integer reviewerId,
    Integer revieweeId,
    LocalDateTime createdAt
) {
}
