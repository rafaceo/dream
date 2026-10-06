package com.demo.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT to send as 'Authorization: Bearer <token>'")
        String accessToken,
        String tokenType,
        @Schema(description = "Token lifetime in seconds")
        long expiresIn
) {
}
