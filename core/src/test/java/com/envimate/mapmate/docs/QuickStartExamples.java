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
import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

public final class QuickStartExamples {
    private static final String YOUR_PACKAGE_TO_SCAN = Email.class.getPackageName();

    @Test
    public void quickStart() {
        //Showcase start instance
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(new Gson()::toJson, new Gson()::fromJson)
                .build();
        //Showcase end instance

        //Showcase start serialization
        final Email email = Email.deserialize(
                EmailAddress.fromStringValue("sender@example.com"),
                EmailAddress.fromStringValue("receiver@example.com"),
                Subject.fromStringValue("Hello"),
                Body.fromStringValue("Hello World!!!")
        );

        final String json = mapMate.serializeToJson(email);
        //Showcase end serialization

        assert json.equals("{\"receiver\":\"receiver@example.com\",\"body\":\"Hello World!!!\",\"sender\":\"sender@example.com\",\"subject\":\"Hello\"}");

        //Showcase start deserialization
        final Email deserializedEmail = mapMate.deserializeJson(json, Email.class);
        //Showcase end deserialization

        assert deserializedEmail.equals(email);
    }
}
