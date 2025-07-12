package com.shishir.ticketmetrics.model;

import java.time.OffsetDateTime;

public record Ticket(
    Integer id,
    String subject,
    OffsetDateTime createdAt
)
{
}
