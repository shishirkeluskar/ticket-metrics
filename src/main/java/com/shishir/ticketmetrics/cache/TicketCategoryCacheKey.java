package com.shishir.ticketmetrics.cache;

import java.time.LocalDateTime;

public record TicketCategoryCacheKey(
    Integer ticketId,
    Integer categoryId,
    LocalDateTime start,
    LocalDateTime end
) {
}
