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
import com.envimate.mapmate.builder.conventional.ConventionalDetectors;
import com.envimate.mapmate.builder.models.annotated.Email;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.builder.models.annotated.Body.body;
import static com.envimate.mapmate.builder.models.annotated.EmailAddress.emailAddress;
import static com.envimate.mapmate.builder.models.annotated.Subject.subject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class AnnotationBuilderTest {

    public static final String EMAIL_JSON = "{" +
            "\"receiver\":\"receiver@example.com\"," +
            "\"body\":\"Hello World!!!\"," +
            "\"sender\":\"sender@example.com\"," +
            "\"subject\":\"Hello\"" +
            "}";
    public static final Email EMAIL = Email.restoreEmail(
            emailAddress("sender@example.com"),
            emailAddress("receiver@example.com"),
            subject("Hello"),
            body("Hello World!!!")
    );

    public static MapMate theAnnotationBasedMapMateInstance() {
        final Gson gson = new Gson();

        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .withDetector(ConventionalDetectors.conventionalDetectorWithAnnotations())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = theAnnotationBasedMapMateInstance().serializer().serializeToJson(EMAIL);
        assertThat(result, is(EMAIL_JSON));
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = theAnnotationBasedMapMateInstance().deserializer().deserializeJson(EMAIL_JSON, Email.class);
        assertThat(result, is(EMAIL));
    }
}
