package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;

public record RatingWithCategoryWeight2(
    Integer ticketId,
    Integer categoryId,
    BigDecimal rating, // 0–5
    BigDecimal weight // from rating_categories
) {
}
