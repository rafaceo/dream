package com.demo.cards.util;

import com.demo.cards.entity.DisplayName;
import com.demo.cards.exception.UnsupportedCardNetworkException;

public final class CardNetworkDetector {

    private CardNetworkDetector() {
    }

    public static DisplayName detect(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return DisplayName.VISA;
        }
        int first2 = Integer.parseInt(cardNumber.substring(0, 2));
        if (first2 >= 51 && first2 <= 55) {
            return DisplayName.MASTERCARD;
        }
        int first4 = Integer.parseInt(cardNumber.substring(0, 4));
        if (first4 >= 2221 && first4 <= 2720) {
            return DisplayName.MASTERCARD;
        }
        if (first2 == 34 || first2 == 37) {
            return DisplayName.AMERICAN_EXPRESS;
        }
        if (cardNumber.startsWith("6")) {
            return DisplayName.DISCOVER;
        }
        throw new UnsupportedCardNetworkException();
    }
}
