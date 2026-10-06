package com.demo.cards.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DisplayName {
    VISA("Visa"),
    MASTERCARD("MasterCard"),
    AMERICAN_EXPRESS("American Express"),
    DISCOVER("Discover");

    private final String label;

    DisplayName(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
