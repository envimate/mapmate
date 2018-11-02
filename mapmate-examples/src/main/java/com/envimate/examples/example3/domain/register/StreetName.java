package com.envimate.examples.example3.domain.register;

public final class StreetName {

    private final String value;

    private StreetName(String value) {
        this.value = value;
    }

    public static final StreetName fromString(String value) {
        return new StreetName(value);
    }

    public String getValue() {
        return this.value;
    }

}
