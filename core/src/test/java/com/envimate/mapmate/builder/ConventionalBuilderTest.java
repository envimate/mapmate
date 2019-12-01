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
import com.envimate.mapmate.builder.models.constructor.GroupTicketRequest;
import com.envimate.mapmate.builder.models.constructor.Name;
import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.EmailDto;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.Body.body;
import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.EmailAddress.anEmailAddress;
import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.EmailDto.emailDto;
import static com.envimate.mapmate.builder.models.conventionalwithclassnamefactories.Subject.aSubject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    public static final EmailDto EMAIL_DTO = emailDto(
            anEmailAddress("sender@example.com"),
            anEmailAddress("receiver@example.com"),
            aSubject("Hello"),
            body("Hello World!!!")
    );

    public static final GroupTicketRequest GROUP_TICKET_REQUEST = new GroupTicketRequest(
            new Name("Aaron"),
            new Name("Abdul"),
            new Name("Abe"),
            new Name("Abel"),
            new Name("Abraham")
    );

    public static final String GROUP_TICKET_REQUEST_JSON = "" +
            "{" +
            "   \"firstParticipant\": \"Aaron\"," +
            "   \"secondParticipant\": \"Abdul\"," +
            "   \"thirdParticipant\": \"Abe\"," +
            "   \"fourthParticipant\": \"Abel\"," +
            "   \"fifthParticipant\": \"Abraham\"" +
            "}";

    public static MapMate theConventionalMapMateInstance() {
        final Gson gson = new Gson();
        return MapMate.aMapMate("com.envimate.mapmate.builder.models.conventional",
                "com.envimate.mapmate.builder.models.conventionalwithclassnamefactories",
                "com.envimate.mapmate.builder.models.constructor")
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = theConventionalMapMateInstance().serializer().serializeToJson(EMAIL);
        assertThat(result, is(EMAIL_JSON));
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = theConventionalMapMateInstance().deserializer().deserializeJson(EMAIL_JSON, Email.class);
        assertThat(result, is(EMAIL));
    }

    @Test
    public void testEmailSerializationClassNameFactories() {
        final String result = theConventionalMapMateInstance().serializer().serializeToJson(EMAIL_DTO);
        assertThat(result, is(EMAIL_JSON));
    }

    @Test
    public void testEmailDeserializationClassNameFactories() {
        final EmailDto result = theConventionalMapMateInstance().deserializer().deserializeJson(
                EMAIL_JSON, EmailDto.class
        );
        assertThat(result, is(EMAIL_DTO));
    }

    @Test
    public void testNameDeserialization() {
        final Name result = theConventionalMapMateInstance().deserializer().deserializeJson("bob", Name.class);
        assertThat(result, is(new Name("bob")));
    }

    @Test
    public void testGroupTicketDeserialization() {
        final GroupTicketRequest result = theConventionalMapMateInstance().deserializer()
                .deserializeJson(GROUP_TICKET_REQUEST_JSON, GroupTicketRequest.class);
        assertThat(result, is(GROUP_TICKET_REQUEST));
    }
}
