package com.demo.cards.util;

public final class CardNumberMasker {

    private static final int VISIBLE_SUFFIX_LENGTH = 4;

    private CardNumberMasker() {
    }

    public static String mask(String rawCardNumber) {
        String suffix = rawCardNumber.substring(rawCardNumber.length() - VISIBLE_SUFFIX_LENGTH);
        return "*".repeat(rawCardNumber.length() - VISIBLE_SUFFIX_LENGTH) + suffix;
    }
}
