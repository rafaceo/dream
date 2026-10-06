package com.demo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Transfer request")
public record TransferRequest(

        @NotNull(message = "fromCardId is required")
        @Schema(description = "Source card id")
        UUID fromCardId,

        @NotNull(message = "toCardId is required")
        @Schema(description = "Destination card id")
        UUID toCardId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "amount must have at most 2 decimal places")
        @Schema(description = "Amount in the card currency, at most 2 decimal places", example = "100.50")
        BigDecimal amount
) {
}
