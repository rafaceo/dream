package com.demo.payment.service;

import com.demo.payment.client.CardsClient;
import com.demo.payment.dto.CardInfo;
import com.demo.payment.dto.PageResponse;
import com.demo.payment.dto.TransferRequest;
import com.demo.payment.dto.TransferResponse;
import com.demo.payment.entity.Transfer;
import com.demo.payment.entity.TransferStatus;
import com.demo.payment.event.TransferCompletedEvent;
import com.demo.payment.exception.CardsServiceUnavailableException;
import com.demo.payment.exception.InvalidTransferException;
import com.demo.payment.exception.TransferNotFoundException;
import com.demo.payment.outbox.OutboxService;
import com.demo.payment.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private static final String ACTIVE = "ACTIVE";

    private final CardsClient cardsClient;
    private final TransferRepository transferRepository;
    private final OutboxService outboxService;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.kafka.transfer-completed-topic}")
    private String transferCompletedTopic;

    @Override
    public TransferOutcome transfer(TransferRequest request) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new InvalidTransferException("fromCardId and toCardId must be different");
        }

        Optional<CardInfo> from;
        Optional<CardInfo> to;
        try {
            from = cardsClient.findCard(request.fromCardId());
            to = cardsClient.findCard(request.toCardId());
        } catch (CardsServiceUnavailableException e) {
            log.error("Cards service unavailable during transfer check", e);
            return failed(request, null, HttpStatus.SERVICE_UNAVAILABLE, "Cards service is unavailable");
        }

        if (from.isEmpty()) {
            return failed(request, null, HttpStatus.NOT_FOUND, "Source card not found");
        }
        String currency = from.get().currency();
        if (to.isEmpty()) {
            return failed(request, currency, HttpStatus.NOT_FOUND, "Destination card not found");
        }
        if (!ACTIVE.equals(from.get().status())) {
            return failed(request, currency, HttpStatus.UNPROCESSABLE_ENTITY, "Source card is not ACTIVE");
        }
        if (!ACTIVE.equals(to.get().status())) {
            return failed(request, currency, HttpStatus.UNPROCESSABLE_ENTITY, "Destination card is not ACTIVE");
        }
        if (!currency.equals(to.get().currency())) {
            return failed(request, currency, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Cards have different currencies: " + currency + " and " + to.get().currency());
        }
        if (from.get().balance().compareTo(request.amount()) < 0) {
            return failed(request, currency, HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }

        Transfer transfer = transactionTemplate.execute(tx -> {
            Transfer saved = transferRepository.save(Transfer.builder()
                    .fromCardId(request.fromCardId())
                    .toCardId(request.toCardId())
                    .amount(request.amount())
                    .currency(currency)
                    .status(TransferStatus.SUCCESS)
                    .build());
            outboxService.enqueue(transferCompletedTopic, saved.getTransferId().toString(),
                    new TransferCompletedEvent(
                            saved.getTransferId(),
                            saved.getFromCardId(),
                            saved.getToCardId(),
                            saved.getAmount(),
                            saved.getCurrency(),
                            Instant.now()));
            return saved;
        });

        return new TransferOutcome(TransferResponse.from(transfer), HttpStatus.OK);
    }

    @Override
    public TransferResponse getTransfer(UUID transferId) {
        return transferRepository.findById(transferId)
                .map(TransferResponse::from)
                .orElseThrow(() -> new TransferNotFoundException(transferId));
    }

    @Override
    public PageResponse<TransferResponse> getTransfers(UUID cardId, TransferStatus status, int page, int size) {
        Specification<Transfer> spec = (root, query, cb) -> cb.conjunction();
        if (cardId != null) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("fromCardId"), cardId),
                    cb.equal(root.get("toCardId"), cardId)));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "transferId")));
        return PageResponse.of(transferRepository.findAll(spec, pageable), TransferResponse::from);
    }

    private TransferOutcome failed(TransferRequest request, String currency, HttpStatus httpStatus, String reason) {
        Transfer transfer = transferRepository.save(Transfer.builder()
                .fromCardId(request.fromCardId())
                .toCardId(request.toCardId())
                .amount(request.amount())
                .currency(currency)
                .status(TransferStatus.FAILED)
                .failureReason(reason)
                .build());
        return new TransferOutcome(TransferResponse.from(transfer), httpStatus);
    }
}
