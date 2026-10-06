package com.demo.cards.service;

import com.demo.cards.dto.CardRequest;
import com.demo.cards.dto.CardResponse;
import com.demo.cards.dto.CardUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface CardService {

    CardResponse addCard(CardRequest request);

    CardResponse getCard(UUID cardId);

    List<CardResponse> getCardsByOwner(UUID ownerId);

    CardResponse updateCard(UUID cardId, CardUpdateRequest request);

    void deleteCard(UUID cardId);
}
