package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Rating(
    Integer id,
    BigDecimal rating,
    Integer ticketId,
    Integer ratingCategoryId,
    Integer reviewerId,
    Integer revieweeId,
    OffsetDateTime createdAt
) {
}
