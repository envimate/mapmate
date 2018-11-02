package com.envimate.examples.example1.domain.auth;

public final class UserName {

    private final String value;

    private UserName(String value) {
        this.value = value;
    }

    public static final UserName fromString(String value) {
        return new UserName(value);
    }

    public String getValue() {
        return this.value;
    }

}
