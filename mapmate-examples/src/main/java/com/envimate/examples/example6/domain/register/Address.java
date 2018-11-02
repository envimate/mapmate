/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.examples.example6.domain.register;


import com.envimate.examples.example6.exceptions.RequiredParameterException;

public final class Address {

    public final StreetName streetName;
    public final HouseNr houseNr;
    public final PostalCode postalCode;
    public final City city;
    public final Country country;

    private Address(StreetName streetName, HouseNr houseNr, PostalCode postalCode, City city, Country country) {
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
        if(this.country == null) {
            throw RequiredParameterException.requiredParameterException("country");
        }
    }
}
