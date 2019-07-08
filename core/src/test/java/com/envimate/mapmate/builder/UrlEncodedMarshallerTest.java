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
import com.envimate.mapmate.marshalling.MarshallingType;
import org.junit.Assert;
import org.junit.Test;

public final class UrlEncodedMarshallerTest {

    public static final String EMAIL_URL_ENCODED = "receiver=receiver%40example.com%3Fbody=Hello%20World%21%21%21%26sender=sender%40example.com%26subject=Hello";

    public static final Email EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );

    public static MapMate urlEncodedConventionalMapMate() {
        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .usingRecipe(UrlEncodedMarshaller.urlEncodedMarshaller())
                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = urlEncodedConventionalMapMate().serializeTo(EMAIL, MarshallingType.urlendcoded());
        Assert.assertEquals(EMAIL_URL_ENCODED, result);
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = urlEncodedConventionalMapMate().deserialize(EMAIL_URL_ENCODED, Email.class, MarshallingType.urlendcoded());
        Assert.assertEquals(EMAIL, result);
    }
}
