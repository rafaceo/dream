package com.demo.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.demo.cards.entity.CardStatus;
import com.demo.cards.entity.CardType;
import com.demo.cards.entity.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record CardRequest(

        @NotNull(message = "cardNumber is required")
        @Pattern(regexp = "\\d{13,19}", message = "cardNumber must contain 13 to 19 digits")
        @Schema(description = "Full card number, 13 to 19 digits", example = "4111111111111111")
        String cardNumber,

        @NotNull(message = "balance is required")
        @PositiveOrZero(message = "balance must not be negative")
        @Digits(integer = 17, fraction = 2, message = "balance must have at most 2 decimal places")
        @Schema(description = "Initial balance, not negative", example = "1000.00")
        BigDecimal balance,

        @NotNull(message = "currency is required")
        Currency currency,

        @NotNull(message = "ownerId is required")
        @Schema(description = "Card owner id")
        UUID ownerId,

        @Schema(description = "Card status, optional")

        CardStatus status,

        @Schema(description = "Card type, optional")

        CardType type
) {
}
