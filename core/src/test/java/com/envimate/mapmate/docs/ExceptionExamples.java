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

package com.envimate.mapmate.docs;

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.mapper.deserialization.validation.AggregatedValidationException;
import com.envimate.mapmate.mapper.deserialization.validation.ValidationError;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.validation.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static java.util.Objects.nonNull;

public final class ExceptionExamples {
    private static final String YOUR_PACKAGE_TO_SCAN = Email.class.getPackageName();
    private static final Gson GSON = new Gson();

    private static final String JSON = "" +
            "{\n" +
            "  \"sender\": \"not-a-valid-sender-value\",\n" +
            "  \"receiver\": \"not-a-valid-receiver-value\"\n" +
            "}";

    @Test
    public void aggregateException() {
        //Showcase start aggregateException
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(GSON::toJson, GSON::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
        //Showcase end aggregateException
        String message = null;
        try {
            mapMate.deserializeJson(JSON, Email.class);
        } catch (final AggregatedValidationException e) {
            e.printStackTrace();
            message = e.getMessage();
            assert message.equals("deserialization encountered validation errors. Validation error at 'receiver', " +
                    "Invalid email address: 'not-a-valid-receiver-value'; " +
                    "Validation error at 'sender', Invalid email address: 'not-a-valid-sender-value'; ");
        }
        assert nonNull(message);
    }

    @Test
    public void mappedExample() {
        //Showcase start mappedException
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(GSON::toJson, GSON::fromJson)
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class,
                        (exception, propertyPath) -> new ValidationError("This is a custom message we are reporting about " + exception.getMessage(), propertyPath))
                .build();
        //Showcase end mappedException

        String message = null;
        try {
            mapMate.deserializeJson(JSON, Email.class);
        } catch (final AggregatedValidationException e) {
            e.printStackTrace();
            message = e.getMessage();
            assert message.equals("deserialization encountered validation errors. Validation error at 'receiver', " +
                    "This is a custom message we are reporting about Invalid email address: 'not-a-valid-receiver-value'; " +
                    "Validation error at 'sender', This is a custom message we are reporting about Invalid email address: 'not-a-valid-sender-value'; ");
        }
        assert nonNull(message);
    }
}
