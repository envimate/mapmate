package com.envimate.examples.example3.domain.register;

import com.envimate.examples.example3.exceptions.CountryNotSupportedException;

public final class Country {

    private final String value;

    private Country(String value) {
        this.value = value;
    }

    public static final Country fromString(String value) {
        final Country country = new Country(value);
        country.validate();
        return country;
    }

    private void validate() {
        if(!this.value.equals("United Kingdom")) {
            throw CountryNotSupportedException.countryNotSupportedException(this.value);
        }
    }

    public String getValue() {
        return this.value;
    }
}
