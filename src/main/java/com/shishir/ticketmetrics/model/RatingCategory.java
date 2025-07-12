package com.shishir.ticketmetrics.model;

import java.math.BigDecimal;

public record RatingCategory(
    Integer id,
    String name,
    BigDecimal weight
) {
}
