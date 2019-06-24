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

import com.envimate.mapmate.builder.models.customconvention.Body;
import com.envimate.mapmate.builder.models.customconvention.Email;
import com.envimate.mapmate.builder.models.customconvention.EmailAddress;
import com.envimate.mapmate.builder.models.customconvention.Subject;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import static com.envimate.mapmate.builder.conventional.ConventionalDetector.conventionalDetector;

public final class CustomConventionalBuilderTest {

    public static final String EMAIL_JSON = "{" +
            "\"receiver\":\"receiver@example.com\"," +
            "\"body\":\"Hello World!!!\"," +
            "\"sender\":\"sender@example.com\"," +
            "\"subject\":\"Hello\"" +
            "}";
    public static final Email EMAIL = Email.restore(
            EmailAddress.deserialize("sender@example.com"),
            EmailAddress.deserialize("receiver@example.com"),
            Subject.deserialize("Hello"),
            Body.deserialize("Hello World!!!")
    );

    public static MapMate theCustomConventionalMapMateInstance() {
        final Gson gson = new Gson();

        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .withDetector(conventionalDetector("serialize",
                        "deserialize",
                        "restore"
                ))
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = theCustomConventionalMapMateInstance().serializer().serializeToJson(EMAIL);
        Assert.assertEquals(EMAIL_JSON, result);
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = theCustomConventionalMapMateInstance().deserializer().deserializeJson(EMAIL_JSON, Email.class);
        Assert.assertEquals(EMAIL, result);
    }
}
