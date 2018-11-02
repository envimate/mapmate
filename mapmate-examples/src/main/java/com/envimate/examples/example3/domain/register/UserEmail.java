package com.envimate.examples.example3.domain.register;

public final class UserEmail {

    private final String value;

    private UserEmail(String value) {
        this.value = value;
    }

    public static final UserEmail fromString(String value) {
        return new UserEmail(value);
    }

    public String getValue() {
        return this.value;
    }

}
