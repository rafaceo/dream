package com.demo.payment.client;

import com.demo.payment.dto.CardInfo;
import com.demo.payment.exception.CardsServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardsClient {

    private final RestClient cardsRestClient;

    public Optional<CardInfo> findCard(UUID cardId) {
        try {
            return Optional.ofNullable(cardsRestClient.get()
                    .uri("/api/cards/{cardId}", cardId)
                    .retrieve()
                    .body(CardInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new CardsServiceUnavailableException(e);
        }
    }
}
