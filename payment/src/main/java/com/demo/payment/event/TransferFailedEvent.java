package com.demo.payment.event;

import java.time.Instant;
import java.util.UUID;

public record TransferFailedEvent(
        UUID transferId,
        String reason,
        Instant occurredAt
) {
}
