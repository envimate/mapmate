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
import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public final class ObjectMapperConventionalBuilderTest {

    public static final String EMAIL_JSON = "{" +
            "\"receiver\":\"receiver@example.com\"," +
            "\"sender\":\"sender@example.com\"," +
            "\"subject\":\"Hello\"," +
            "\"body\":\"Hello World!!!\"" +
            "}";
    public static final Email EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );

    public static MapMate theConventionalMapMateInstanceWithObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .usingJsonMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = theConventionalMapMateInstanceWithObjectMapper().serializeToJson(EMAIL);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = theConventionalMapMateInstanceWithObjectMapper().deserializeJson(EMAIL_JSON, Email.class);
        Assert.assertEquals(EMAIL, result);
    }
}
