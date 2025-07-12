package com.shishir.ticketmetrics.model;

import java.time.LocalDateTime;

public record Ticket(
    Integer id,
    String subject,
    LocalDateTime createdAt
)
{
}
