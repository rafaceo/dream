package com.demo.cards.service;

import com.demo.cards.entity.TransferResult;

public record TransferProcessingResult(TransferResult result, String reason) {

    public boolean isRejected() {
        return result == TransferResult.REJECTED;
    }
}
