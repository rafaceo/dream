package com.demo.payment.service;

import com.demo.payment.entity.Transfer;
import com.demo.payment.entity.TransferStatus;
import com.demo.payment.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferStatusService {

    private static final int MAX_REASON_LENGTH = 255;

    private final TransferRepository transferRepository;

    @Transactional
    public void markFailed(UUID transferId, String reason) {
        Optional<Transfer> found = transferRepository.findById(transferId);
        if (found.isEmpty()) {
            log.warn("transfer.failed for unknown transfer {}", transferId);
            return;
        }
        Transfer transfer = found.get();
        if (transfer.getStatus() == TransferStatus.FAILED) {
            return;
        }
        String message = "Rejected by cards: " + reason;
        transfer.setStatus(TransferStatus.FAILED);
        transfer.setFailureReason(message.length() > MAX_REASON_LENGTH ? message.substring(0, MAX_REASON_LENGTH) : message);
        log.info("Transfer {} marked FAILED: {}", transferId, transfer.getFailureReason());
    }
}
