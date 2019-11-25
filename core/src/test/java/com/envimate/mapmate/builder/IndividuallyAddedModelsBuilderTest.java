/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.mapmate.builder;

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.builder.models.customconvention.Body;
import com.envimate.mapmate.builder.models.customconvention.Email;
import com.envimate.mapmate.builder.models.customconvention.EmailAddress;
import com.envimate.mapmate.builder.models.customconvention.Subject;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.envimate.mapmate.deserialization.Deserializer;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.builder.conventional.ConventionalDetectors.conventionalDetector;
import static com.envimate.mapmate.builder.recipes.manualregistry.ManualRegistry.manuallyRegisteredTypes;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test describes 2 ways of adding Custom Primitives and Serialized Objects individually to the MapMate instance.
 * If you chose to follow the standards we have decided, which are described in README.md#default-conventions-explained
 * then you can use the method described in
 * {@link #theIndividuallyAddedTypesMapMateConventional() theIndividuallyAddedTypesMapMateConventional}
 * and simply register your class. The methods will then be auto-discovered for you.
 * <p>
 * If, however, you chose to change those, like shown in the package
 * {@code com.envimate.mapmate.builder.models.customconvention}
 * take a look at {@link #theIndividuallyAddedTypesMapMate() theIndividuallyAddedTypesMapMate} where you can see
 * how you can add Custom Primitives and Serialized Objects, together with their serialization and deserialization
 * methods.
 */
public final class IndividuallyAddedModelsBuilderTest {
    public static final String EMAIL_JSON = "{" +
            "\"receiver\":\"receiver@example.com\"," +
            "\"body\":\"Hello World!!!\"," +
            "\"sender\":\"sender@example.com\"," +
            "\"subject\":\"Hello\"" +
            "}";

    public static final com.envimate.mapmate.builder.models.conventional.Email CONVENTIONAL_EMAIL =
            com.envimate.mapmate.builder.models.conventional.Email.deserialize(
                    com.envimate.mapmate.builder.models.conventional.EmailAddress.fromStringValue("sender@example.com"),
                    com.envimate.mapmate.builder.models.conventional.EmailAddress.fromStringValue("receiver@example.com"),
                    com.envimate.mapmate.builder.models.conventional.Subject.fromStringValue("Hello"),
                    com.envimate.mapmate.builder.models.conventional.Body.fromStringValue("Hello World!!!")
            );

    public static final Email EMAIL =
            Email.restore(
                    EmailAddress.deserialize("sender@example.com"),
                    EmailAddress.deserialize("receiver@example.com"),
                    Subject.deserialize("Hello"),
                    Body.deserialize("Hello World!!!")
            );

    private static final Gson GSON = new Gson();

    public static MapMate theIndividuallyAddedTypesMapMateConventional() {
        return MapMate.aMapMate()
                .usingRecipe(manuallyRegisteredTypes()
                        .withSerializedObjects(
                                com.envimate.mapmate.builder.models.conventional.Email.class
                        )
                        .withCustomPrimitives(
                                com.envimate.mapmate.builder.models.conventional.EmailAddress.class,
                                com.envimate.mapmate.builder.models.conventional.Subject.class,
                                com.envimate.mapmate.builder.models.conventional.Body.class)
                )
                .usingJsonMarshaller(GSON::toJson, GSON::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    public static MapMate theIndividuallyAddedTypesMapMate() {
        final Class<Body>
                customConventionBody = Body.class;
        return MapMate.aMapMate()
                .usingRecipe(manuallyRegisteredTypes()
                        .withSerializedObject(Email.class, Email.class.getFields(), "restore")
                        .withCustomPrimitive(EmailAddress.class, EmailAddress::serialize, EmailAddress::deserialize)
                        .withCustomPrimitive(Subject.class, Subject::serialize, Subject::deserialize)
                        .withCustomPrimitive(customConventionBody, Body::serialize, Body::deserialize)
                )
                .usingJsonMarshaller(GSON::toJson, GSON::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    public static MapMate theIndividuallyAddedTypesMapMate1() {
        final Gson gson = new Gson();

        return MapMate.aMapMate()
                .withDetector(conventionalDetector(
                        "serialize", "" +
                                "deserialize",
                        "restore",
                        ".*")
                )
                .usingRecipe(manuallyRegisteredTypes()
                        .withSerializedObjects(Email.class)
                        .withCustomPrimitives(EmailAddress.class, Subject.class, Body.class)
                )
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerializationConventional() {
        final String result = theIndividuallyAddedTypesMapMateConventional()
                .serializer()
                .serializeToJson(CONVENTIONAL_EMAIL);
        assertThat(result, is(EMAIL_JSON));
    }

    @Test
    public void testEmailDeserializationConventional() {
        final com.envimate.mapmate.builder.models.conventional.Email result = theIndividuallyAddedTypesMapMateConventional()
                .deserializer()
                .deserializeJson(EMAIL_JSON, com.envimate.mapmate.builder.models.conventional.Email.class);
        assertThat(result, is(CONVENTIONAL_EMAIL));
    }

    @Test
    public void testEmailSerialization() {
        final String result = theIndividuallyAddedTypesMapMate().serializer().serializeToJson(EMAIL);
        assertThat(result, is(EMAIL_JSON));
    }

    @Test
    public void testEmailDeserialization() {
        final Deserializer deserializer = theIndividuallyAddedTypesMapMate().deserializer();
        final Email result = deserializer.deserializeJson(EMAIL_JSON, Email.class);
        assertThat(result, is(EMAIL));
    }

    @Test
    public void testEmailSerialization1() {
        final String result = theIndividuallyAddedTypesMapMate1().serializer().serializeToJson(EMAIL);
        assertThat(result, is(EMAIL_JSON));
    }

    @Test
    public void testEmailDeserialization1() {
        final Deserializer deserializer = theIndividuallyAddedTypesMapMate1().deserializer();
        final Email result = deserializer.deserializeJson(EMAIL_JSON, Email.class);
        assertThat(result, is(EMAIL));
    }
}
