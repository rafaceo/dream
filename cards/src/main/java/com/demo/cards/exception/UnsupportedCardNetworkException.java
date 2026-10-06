package com.demo.cards.exception;

public class UnsupportedCardNetworkException extends RuntimeException {

    public UnsupportedCardNetworkException() {
        super("cardNumber must start with digits of a supported network: Visa, MasterCard, American Express, Discover");
    }
}
