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

package com.envimate.mapmate.examples;

import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.examples.domain.*;
import com.envimate.mapmate.serialization.Serializer;
import com.envimate.mapmate.validation.AggregatedValidationException;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.includingAll;

@SuppressWarnings("ALL")
@Ignore
public final class Examples {
    private final Injector injector = Guice.createInjector(new ExampleModule());
    private Serializer serializer;
    private Deserializer deserializer;

    @Test
    public void example_serializing() {
        final Person queen = Person.person(
                FullName.fullName(
                        FirstName.fromString("Beatrix"),
                        LastName.fromString("Armgard"),
                        LastNamePrefix.fromString("van"),
                        new MiddleName[]{MiddleName.fromString("Wilhelmina")}
                ),
                Address.address(
                        StreetName.fromString("Dam"),
                        HouseNumber.fromString("1"),
                        ZipCode.fromString("1012KB"),
                        CityName.fromString("Amsterdam"),
                        Region.fromString("Noord-Holland"),
                        Country.fromString("Netherlands")

                ));

        final String result = this.serializer.serialize(queen);
        System.out.println(result);
    }

    @Test
    public void example_deserializing() {
        final String json = "{\"fullName\":{" +
                "\"middleNames\":[\"Wilhelmina\", \"Petronella\"]," +
                "\"firstName\":\"Beatrix\"," +
                "\"lastNamePrefix\":\"van\"," +
                "\"lastName\":\"Armgard\"}," +
                "\"address\":{" +
                "\"houseNumber\":\"1\"," +
                "\"zipCode\":\"1012KB\"," +
                "\"country\":\"Netherlands\"," +
                "\"streetName\":\"Dam\"," +
                "\"region\":\"Noord-Holland\"," +
                "\"city\":\"Amsterdam\"}}";

        final Person queen = this.deserializer.deserialize(json, Person.class);

        System.out.println(queen.fullName.textual());
        System.out.println(queen.address.textual());
    }

    @Test
    public void example_deserializingWithValidations() {
        final String json = "{\"fullName\":{" +
                "\"firstName\":\"Be\"}," +
                "\"address\":{" +
                "\"houseNumber\":\"1\"," +
                "\"zipCode\":\"1012KB\"," +
                "\"country\":\"Netherlands\"," +
                "\"streetName\":\"Da\"," +
                "\"region\":\"Noord-Holland\"," +
                "\"city\":\"Amsterdam\"}}";

        try {
            this.deserializer.deserialize(json, ValidPerson.class);
        } catch (AggregatedValidationException e) {
            System.out.println(e.getValidationErrors());
        }
    }

    @Test
    public void example_complexPerson() {
        final DeserializationDTOMethod deserializerAdapter = complexPersonAdapter();

        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.mapmate.examples.domain")
                .forCustomPrimitives()
                .filteredBy(includingAll())
                .thatAre().deserializedUsingTheMethodNamed("fromString")
                .thatScansThePackage("com.envimate.mapmate.examples.domain")
                .forDataTransferObjects()
                .filteredBy(includingAll())
                .excluding(ComplexPerson.class)
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .withDataTransferObject(ComplexPerson.class)
                .deserializedUsing(deserializerAdapter)
                .build();

        final String json = "{\n" +
                "  \"firstNames\": [\"Patrick\", \"Richard\", \"Nune\"],\n" +
                "  \"addresses\": [\n" +
                "    {\n" +
                "      \"streetName\": \"KieholzStraße\",\n" +
                "      \"houseNumber\": \"403a\",\n" +
                "      \"zipCode\": \"12345\",\n" +
                "      \"city\": \"Berlin\",\n" +
                "      \"region\": \"Berlin-north\",\n" +
                "      \"country\": \"Berlinnia\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"streetName\": \"Dr ehaushof\",\n" +
                "      \"houseNumber\": \"83\",\n" +
                "      \"zipCode\": \"5042EY\",\n" +
                "      \"city\": \"Tilburg\",\n" +
                "      \"region\": \"Noord-brabant\",\n" +
                "      \"country\": \"Netherlands\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        final ComplexPerson personWithComplexes = deserializer.deserialize(json, ComplexPerson.class);

        System.out.println(personWithComplexes);
    }

    private DeserializationDTOMethod complexPersonAdapter() {


        return new DeserializationDTOMethod() {
            @Override
            public Object deserialize(Class<?> targetType, Map<String, Object> elements) throws Exception {
                FirstName[] firstNames = (FirstName[]) elements.get("firstNames");
                Address[] addresses = (Address[]) elements.get("addresses");
                return ComplexPerson.person(
                        Arrays.asList(firstNames),
                        Arrays.asList(addresses));
            }

            @Override
            public Map<String, Class<?>> elements(Class<?> targetType) {
                final Map<String, Class<?>> elements = new HashMap<>();
                elements.put("firstNames", FirstName[].class);
                elements.put("addresses", Address[].class);
                return elements;
            }
        };
    }

    @Before
    public void before() {
        this.serializer = this.injector.getInstance(Serializer.class);
        this.deserializer = this.injector.getInstance(Deserializer.class);
    }

}
