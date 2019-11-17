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
import com.envimate.mapmate.builder.scanning.DefaultPackageScanner;
import com.envimate.mapmate.definitions.DefinitionNotFoundException;
import com.envimate.mapmate.builder.models.excluded.Body;
import com.envimate.mapmate.builder.models.excluded.Email;
import com.envimate.mapmate.builder.models.excluded.EmailAddress;
import com.envimate.mapmate.builder.models.excluded.Subject;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

public final class ConventionalBuilderExclusionTest {

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

    public static MapMate theConventionalMapMateInstance() {
        final Gson gson = new Gson();

        return MapMate.aMapMate(DefaultPackageScanner.defaultPackageScanner(
                List.of("com.envimate.mapmate.builder.models"),
                List.of(),
                List.of("com.envimate.mapmate.builder.models.excluded"),
                List.of())
        )
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test(expected = DefinitionNotFoundException.class)
    public void testEmailSerialization() {
        theConventionalMapMateInstance().serializer().serializeToJson(EMAIL);
    }

    @Test(expected = DefinitionNotFoundException.class)
    public void testEmailDeserialization() {
        theConventionalMapMateInstance().deserializer().deserializeJson(EMAIL_JSON, Email.class);
    }
}
