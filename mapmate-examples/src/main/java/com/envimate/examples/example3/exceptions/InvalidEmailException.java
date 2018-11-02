package com.envimate.examples.example3.exceptions;

public final class InvalidEmailException extends RuntimeException {

    private final String email;

    private InvalidEmailException(String msg, String email) {
        super(msg);
        this.email = email;
    }

    public static final InvalidEmailException invalidEmailException(final String email) {
        final String msg = String.format("email [%s] is invalid", email);
        return new InvalidEmailException(msg, email);
    }

}
