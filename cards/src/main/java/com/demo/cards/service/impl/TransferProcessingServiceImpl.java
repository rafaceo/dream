package com.demo.cards.service.impl;

import com.demo.cards.entity.Card;
import com.demo.cards.entity.CardStatus;
import com.demo.cards.entity.ProcessedTransfer;
import com.demo.cards.entity.TransferResult;
import com.demo.cards.event.TransferCompletedEvent;
import com.demo.cards.repository.CardRepository;
import com.demo.cards.repository.ProcessedTransferRepository;
import com.demo.cards.service.TransferProcessingResult;
import com.demo.cards.service.TransferProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransferProcessingServiceImpl implements TransferProcessingService {

    private final CardRepository cardRepository;
    private final ProcessedTransferRepository processedTransferRepository;

    @Override
    public TransferProcessingResult apply(TransferCompletedEvent event) {
        Optional<ProcessedTransfer> alreadyProcessed = processedTransferRepository.findById(event.transferId());
        if (alreadyProcessed.isPresent()) {
            log.info("Transfer {} already processed, skipping", event.transferId());
            return new TransferProcessingResult(alreadyProcessed.get().getResult(), alreadyProcessed.get().getReason());
        }

        Map<UUID, Card> cards = cardRepository
                .findAllByIdForUpdate(List.of(event.fromCardId(), event.toCardId())).stream()
                .collect(Collectors.toMap(Card::getCardId, Function.identity()));
        Card from = cards.get(event.fromCardId());
        Card to = cards.get(event.toCardId());

        String rejection = findRejectionReason(event, from, to);
        if (rejection != null) {
            log.warn("Transfer {} rejected: {}", event.transferId(), rejection);
            processedTransferRepository.save(ProcessedTransfer.builder()
                    .transferId(event.transferId())
                    .result(TransferResult.REJECTED)
                    .reason(rejection)
                    .build());
            return new TransferProcessingResult(TransferResult.REJECTED, rejection);
        }

        from.setBalance(from.getBalance().subtract(event.amount()));
        to.setBalance(to.getBalance().add(event.amount()));
        processedTransferRepository.save(ProcessedTransfer.builder()
                .transferId(event.transferId())
                .result(TransferResult.APPLIED)
                .build());
        log.info("Transfer {} applied: {} {} from {} to {}",
                event.transferId(), event.amount(), event.currency(), event.fromCardId(), event.toCardId());
        return new TransferProcessingResult(TransferResult.APPLIED, null);
    }

    private String findRejectionReason(TransferCompletedEvent event, Card from, Card to) {
        if (from == null) {
            return "source card not found";
        }
        if (to == null) {
            return "destination card not found";
        }
        if (from.getStatus() != CardStatus.ACTIVE) {
            return "source card is not ACTIVE";
        }
        if (to.getStatus() != CardStatus.ACTIVE) {
            return "destination card is not ACTIVE";
        }
        if (from.getCurrency() != to.getCurrency()) {
            return "cards have different currencies";
        }
        if (event.amount() == null || event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return "amount must be positive";
        }
        if (from.getBalance().compareTo(event.amount()) < 0) {
            return "insufficient funds";
        }
        return null;
    }
}
