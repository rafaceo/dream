package com.demo.payment.exception;

public class CardsServiceUnavailableException extends RuntimeException {

    public CardsServiceUnavailableException(Throwable cause) {
        super("Cards service is unavailable", cause);
    }
}
