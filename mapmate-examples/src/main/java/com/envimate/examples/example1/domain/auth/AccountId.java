package com.envimate.examples.example1.domain.auth;

public final class AccountId {

    private final String value;

    private AccountId(String value) {
        this.value = value;
    }

    public static final AccountId fromString(String value) {
        return new AccountId(value);
    }

    public String getValue() {
        return this.value;
    }

}
