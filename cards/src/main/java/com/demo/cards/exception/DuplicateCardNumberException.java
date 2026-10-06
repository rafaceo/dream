package com.demo.cards.exception;

public class DuplicateCardNumberException extends RuntimeException {

    public DuplicateCardNumberException() {
        super("Card with this cardNumber already exists");
    }
}
