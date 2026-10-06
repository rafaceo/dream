package com.demo.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CardInfo(
        UUID cardId,
        BigDecimal balance,
        String currency,
        String status
) {
}
