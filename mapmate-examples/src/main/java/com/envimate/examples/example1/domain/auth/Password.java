package com.envimate.examples.example1.domain.auth;

public final class Password {

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static final Password fromString(String value) {
        return new Password(value);
    }

    public String getValue() {
        return this.value;
    }
}
