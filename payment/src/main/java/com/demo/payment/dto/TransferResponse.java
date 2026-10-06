package com.demo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.demo.payment.entity.Transfer;
import com.demo.payment.entity.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        @Schema(description = "Transfer id")
        UUID transferId,
        UUID fromCardId,
        UUID toCardId,
        BigDecimal amount,
        String currency,
        @Schema(description = "Current transfer status")
        TransferStatus status,
        @Schema(description = "Why the transfer failed, null otherwise")
        String failureReason,
        Instant createdAt
) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getTransferId(),
                transfer.getFromCardId(),
                transfer.getToCardId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus(),
                transfer.getFailureReason(),
                transfer.getCreatedAt());
    }
}
