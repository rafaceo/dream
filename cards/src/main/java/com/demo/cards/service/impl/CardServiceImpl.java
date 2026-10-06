package com.demo.cards.service.impl;

import com.demo.cards.dto.CardRequest;
import com.demo.cards.dto.CardResponse;
import com.demo.cards.dto.CardUpdateRequest;
import com.demo.cards.entity.Card;
import com.demo.cards.entity.CardStatus;
import com.demo.cards.entity.CardType;
import com.demo.cards.exception.CardNotFoundException;
import com.demo.cards.exception.DuplicateCardNumberException;
import com.demo.cards.repository.CardRepository;
import com.demo.cards.service.CardService;
import com.demo.cards.util.CardNetworkDetector;
import com.demo.cards.util.CardNumberHasher;
import com.demo.cards.util.CardNumberMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardNumberHasher cardNumberHasher;

    @Override
    public CardResponse addCard(CardRequest request) {
        var displayName = CardNetworkDetector.detect(request.cardNumber());
        String cardNumberHash = cardNumberHasher.hash(request.cardNumber());
        if (cardRepository.existsByCardNumberHash(cardNumberHash)) {
            throw new DuplicateCardNumberException();
        }
        Card card = Card.builder()
                .cardNumber(CardNumberMasker.mask(request.cardNumber()))
                .cardNumberHash(cardNumberHash)
                .balance(request.balance())
                .currency(request.currency())
                .ownerId(request.ownerId())
                .status(request.status() != null ? request.status() : CardStatus.ACTIVE)
                .type(request.type() != null ? request.type() : CardType.DEBIT)
                .displayName(displayName)
                .build();
        return CardResponse.from(cardRepository.saveAndFlush(card));
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getCard(UUID cardId) {
        return CardResponse.from(findCardOrThrow(cardId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByOwner(UUID ownerId) {
        return cardRepository.findAllByOwnerId(ownerId).stream()
                .map(CardResponse::from)
                .toList();
    }

    @Override
    public CardResponse updateCard(UUID cardId, CardUpdateRequest request) {
        Card card = findCardOrThrow(cardId);
        card.setStatus(request.status());
        return CardResponse.from(cardRepository.saveAndFlush(card));
    }

    @Override
    public void deleteCard(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
    }

    private Card findCardOrThrow(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }
}
