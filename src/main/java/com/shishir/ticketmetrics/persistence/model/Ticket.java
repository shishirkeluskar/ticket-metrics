package com.shishir.ticketmetrics.persistence.model;

import java.time.LocalDateTime;

public record Ticket(
    Integer id,
    String subject,
    LocalDateTime createdAt
)
{
}
