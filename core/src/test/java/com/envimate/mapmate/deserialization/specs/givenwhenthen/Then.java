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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.StringContains;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Then {
    private final Object deserializationResult;
    private final Exception exception;

    public Then theDeserializedObjectIs(final Object expected) {
        assertThat(this.deserializationResult, is(expected));
        return this;
    }

    public Then anExceptionIsThrownWithAMessageContaining(final String message) {
        assertThat(this.exception.getMessage(), StringContains.containsString(message));
        return this;
    }
}
