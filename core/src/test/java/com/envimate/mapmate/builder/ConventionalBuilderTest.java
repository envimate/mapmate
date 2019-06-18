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
import com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.EmailDto;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.Body.body;
import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.EmailDto.email;
import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.EmailAddress.anEmailAddress;
import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.Subject.aSubject;

public final class ConventionalBuilderTest {

    public static final String EMAIL_JSON = "{" +
            "\"receiver\":\"receiver@example.com\"," +
            "\"body\":\"Hello World!!!\"," +
            "\"sender\":\"sender@example.com\"," +
            "\"subject\":\"Hello\"" +
            "}";
    public static final Email EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );

    public static final EmailDto EMAIL_DTO = email(
            anEmailAddress("sender@example.com"),
            anEmailAddress("receiver@example.com"),
            aSubject("Hello"),
            body("Hello World!!!")
    );

    public static MapMate theConventionalMapMateInstance() {
        final Gson gson = new Gson();
        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .usingJsonMarshallers(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = theConventionalMapMateInstance().serializer().serializeToJson(EMAIL);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = theConventionalMapMateInstance().deserializer().deserializeJson(EMAIL_JSON, Email.class);
        Assert.assertEquals(EMAIL, result);
    }

    @Test
    public void testEmailSerializationClassNameFactories() {
        final String result = theConventionalMapMateInstance().serializer().serializeToJson(EMAIL_DTO);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserializationClassNameFactories() {
        final EmailDto result = theConventionalMapMateInstance().deserializer().deserializeJson(
                EMAIL_JSON, EmailDto.class
        );
        Assert.assertEquals(EMAIL_DTO, result);
    }
}
