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

import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.envimate.mapmate.deserialization.Deserializer;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import static com.envimate.mapmate.builder.conventional.ConventionalDetector.conventionalDetector;

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

    public static final Email CONVENTIONAL_EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );

    public static final com.envimate.mapmate.builder.models.customconvention.Email EMAIL =
            com.envimate.mapmate.builder.models.customconvention.Email.restore(
                    com.envimate.mapmate.builder.models.customconvention.EmailAddress.deserialize("sender@example.com"),
                    com.envimate.mapmate.builder.models.customconvention.EmailAddress.deserialize("receiver@example.com"),
                    com.envimate.mapmate.builder.models.customconvention.Subject.deserialize("Hello"),
                    com.envimate.mapmate.builder.models.customconvention.Body.deserialize("Hello World!!!")
            );

    private static final Gson GSON = new Gson();

    public static MapMate theIndividuallyAddedTypesMapMateConventional() {
        return MapMate.aMapMate()
                .withSerializedObjects(Email.class)
                .withCustomPrimitives(EmailAddress.class, Subject.class, Body.class)
                .usingJsonMarshallers(GSON::toJson, GSON::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    public static MapMate theIndividuallyAddedTypesMapMate() {
        try {
            final Class<com.envimate.mapmate.builder.models.customconvention.EmailAddress>
                    customConventionEmail = com.envimate.mapmate.builder.models.customconvention.EmailAddress.class;
            final Class<com.envimate.mapmate.builder.models.customconvention.Subject>
                    customConventionSubject = com.envimate.mapmate.builder.models.customconvention.Subject.class;
            final Class<com.envimate.mapmate.builder.models.customconvention.Body>
                    customConventionBody = com.envimate.mapmate.builder.models.customconvention.Body.class;
            return MapMate.aMapMate()
                    .withSerializedObject(com.envimate.mapmate.builder.models.customconvention.Email.class,
                            com.envimate.mapmate.builder.models.customconvention.Email.class.getFields(),
                            com.envimate.mapmate.builder.models.customconvention.Email.class.getMethod(
                                    "restore",
                                    customConventionEmail,
                                    customConventionEmail,
                                    customConventionSubject,
                                    customConventionBody)
                    )
                    .withCustomPrimitive(
                            customConventionEmail,
                            customConventionEmail.getMethod("serialize"),
                            customConventionEmail.getMethod("deserialize", String.class)
                    )
                    .withCustomPrimitive(
                            customConventionSubject,
                            customConventionSubject.getMethod("serialize"),
                            customConventionSubject.getMethod("deserialize", String.class)
                    )
                    .withCustomPrimitive(
                            customConventionBody,
                            customConventionBody.getMethod("serialize"),
                            customConventionBody.getMethod("deserialize", String.class)
                    )
                    .usingJsonMarshallers(GSON::toJson, GSON::fromJson)
                    .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                    .build();
        } catch (final NoSuchMethodException e) {
            throw new UnsupportedOperationException("Could not find method", e);
        }
    }

    public static MapMate theIndividuallyAddedTypesMapMate1() {
        final Gson gson = new Gson();

        return MapMate.aMapMate()
                .withSerializedObjects(com.envimate.mapmate.builder.models.customconvention.Email.class)
                .withCustomPrimitives(
                        com.envimate.mapmate.builder.models.customconvention.EmailAddress.class,
                        com.envimate.mapmate.builder.models.customconvention.Subject.class,
                        com.envimate.mapmate.builder.models.customconvention.Body.class
                )
                .withDetector(conventionalDetector(
                        "serialize", "" +
                                "deserialize",
                        "restore",
                        ".*")
                )
                .usingJsonMarshallers(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerializationConventional() {
        final String result = theIndividuallyAddedTypesMapMateConventional()
                .serializer()
                .serializeToJson(CONVENTIONAL_EMAIL);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserializationConventional() {
        final Email result = theIndividuallyAddedTypesMapMateConventional()
                .deserializer()
                .deserializeJson(EMAIL_JSON, Email.class);
        Assert.assertEquals(CONVENTIONAL_EMAIL, result);
    }

    @Test
    public void testEmailSerialization() {
        final String result = theIndividuallyAddedTypesMapMate().serializer().serializeToJson(EMAIL);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserialization() {
        final Deserializer deserializer = theIndividuallyAddedTypesMapMate().deserializer();
        final com.envimate.mapmate.builder.models.customconvention.Email result = deserializer
                .deserializeJson(EMAIL_JSON, com.envimate.mapmate.builder.models.customconvention.Email.class);
        Assert.assertEquals(EMAIL, result);
    }

    @Test
    public void testEmailSerialization1() {
        final String result = theIndividuallyAddedTypesMapMate1().serializer().serializeToJson(EMAIL);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserialization1() {
        final Deserializer deserializer = theIndividuallyAddedTypesMapMate1().deserializer();
        final com.envimate.mapmate.builder.models.customconvention.Email result = deserializer
                .deserializeJson(EMAIL_JSON, com.envimate.mapmate.builder.models.customconvention.Email.class);
        Assert.assertEquals(EMAIL, result);
    }
}
