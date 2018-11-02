package com.envimate.examples.example3.domain.register;

public final class HouseNr {

    private final String value;

    private HouseNr(String value) {
        this.value = value;
    }

    public static final HouseNr fromString(String value) {
        return new HouseNr(value);
    }

    public String getValue() {
        return this.value;
    }

}
