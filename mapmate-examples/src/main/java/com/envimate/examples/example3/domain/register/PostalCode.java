package com.envimate.examples.example3.domain.register;

public final class PostalCode {

    private final String value;

    private PostalCode(String value) {
        this.value = value;
    }

    public static final PostalCode fromString(String value) {
        return new PostalCode(value);
    }

    public String getValue() {
        return this.value;
    }

}
