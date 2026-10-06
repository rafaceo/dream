package com.demo.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.demo.cards.entity.CardStatus;
import jakarta.validation.constraints.NotNull;

public record CardUpdateRequest(

        @NotNull(message = "status is required")
        @Schema(description = "New card status")
        CardStatus status
) {
}
