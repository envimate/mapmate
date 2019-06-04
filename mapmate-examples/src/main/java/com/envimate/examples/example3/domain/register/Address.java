package com.envimate.examples.example3.domain.register;

import com.envimate.examples.example3.exceptions.MissingParameterException;

import java.util.LinkedList;
import java.util.List;

public final class Address {

    public final StreetName streetName;
    public final HouseNr houseNr;
    public final PostalCode postalCode;
    public final City city;
    public final Country country;

    private Address(final StreetName streetName, final HouseNr houseNr, final PostalCode postalCode, final City city, final Country country) {
        this.streetName = streetName;
        this.houseNr = houseNr;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public static final Address address(
            final StreetName streetName,
            final HouseNr houseNr,
            final PostalCode postalCode,
            final City city,
            final Country country
    ) {
        final Address address = new Address(streetName, houseNr, postalCode, city, country);
        address.validate();
        return address;
    }

    private final void validate() {
        final List<String> missingFields = new LinkedList<>();
        if (this.country == null) {
            missingFields.add("country");
        }
        if (this.streetName == null) {
            missingFields.add("streetName");
        }
        if (this.postalCode == null) {
            missingFields.add("postalCode");
        }

        if (!missingFields.isEmpty()) {
            throw MissingParameterException.missingParameterException(missingFields);
        }
    }
}
