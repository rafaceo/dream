package com.demo.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.demo.cards.entity.Card;
import com.demo.cards.entity.CardStatus;
import com.demo.cards.entity.CardType;
import com.demo.cards.entity.Currency;
import com.demo.cards.entity.DisplayName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CardResponse(
        UUID cardId,
        @Schema(description = "Masked card number")
        String cardNumber,
        BigDecimal balance,
        Currency currency,
        UUID ownerId,
        CardStatus status,
        CardType type,
        @Schema(description = "Card network name detected from the number")
        DisplayName displayName,
        Instant createdAt,
        Instant updatedAt
) {

    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getCardId(),
                card.getCardNumber(),
                card.getBalance(),
                card.getCurrency(),
                card.getOwnerId(),
                card.getStatus(),
                card.getType(),
                card.getDisplayName(),
                card.getCreatedAt(),
                card.getUpdatedAt());
    }
}
