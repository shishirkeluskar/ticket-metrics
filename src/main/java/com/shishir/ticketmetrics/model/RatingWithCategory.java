package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RatingWithCategory(
    Integer categoryId,
    BigDecimal rating,
    LocalDateTime timestamp
) {

}
