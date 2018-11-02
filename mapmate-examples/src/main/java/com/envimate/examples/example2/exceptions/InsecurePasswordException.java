package com.envimate.examples.example2.exceptions;

public final class InsecurePasswordException extends RuntimeException{

    private final String value;

    private InsecurePasswordException(String msg, String value) {
        super(msg);
        this.value = value;
    }

    public static InsecurePasswordException insecurePasswordException(String value) {
        final String msg = String.format("password does not contain special characters");
        return new InsecurePasswordException(msg, value);
    }
}
