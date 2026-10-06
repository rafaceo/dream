package com.demo.payment.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID transferId,
        UUID fromCardId,
        UUID toCardId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}
