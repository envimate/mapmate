package com.envimate.examples.example3.domain.register;

public final class City {

    private final String value;

    private City(String value) {
        this.value = value;
    }

    public static final City fromString(String value) {
        return new City(value);
    }

    public String getValue() {
        return this.value;
    }
}
