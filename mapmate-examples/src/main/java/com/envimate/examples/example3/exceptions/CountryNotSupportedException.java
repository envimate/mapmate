package com.envimate.examples.example3.exceptions;

public final class CountryNotSupportedException extends RuntimeException {
    private CountryNotSupportedException(String msg) {
        super(msg);
    }

    public static CountryNotSupportedException countryNotSupportedException(String country) {
        String msg = String.format("country '%s' is not supported", country);
        return new CountryNotSupportedException(msg);
    }
}
