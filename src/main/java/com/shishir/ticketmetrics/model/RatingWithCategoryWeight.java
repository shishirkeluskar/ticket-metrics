package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RatingWithCategoryWeight(
    Integer ticketId,
    Integer categoryId,
    BigDecimal rating, // 0–5
    BigDecimal weight, // from rating_categories
    LocalDateTime ticketCreatedAt
) {
}
