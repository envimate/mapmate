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

package com.envimate.mapmate.deserialization.specs.givenwhenthen;

import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("deprecation")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Then {
    private final Object deserializationResult;
    private final Exception exception;

    public Then theDeserializedObjectIsTheFullyInitializedExampleDto() {
        final AComplexType expected = AComplexType.aComplexType(
                AString.fromString("asdf"),
                AString.fromString("qwer"),
                ANumber.fromInt(1),
                ANumber.fromInt(5));
        assertThat(this.deserializationResult, is(expected));
        return this;
    }

    public Then anExceptionIsThrownWithTheMessage(final String message) {
        assertThat(this.exception.getMessage(), is(message));
        return this;
    }
}
