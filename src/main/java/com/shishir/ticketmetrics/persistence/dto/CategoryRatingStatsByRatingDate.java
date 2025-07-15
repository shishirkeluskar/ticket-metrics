package com.shishir.ticketmetrics.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CategoryRatingStatsByRatingDate(
    Integer categoryId,
    LocalDate ratingDate,
    Long ratingCount,
    BigDecimal ratingSum,
    BigDecimal ratingAverage
) {

}
