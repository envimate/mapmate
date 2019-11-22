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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.definitions.universal.UniversalType;

import static com.envimate.mapmate.definitions.universal.UniversalType.describe;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;

public final class WrongInputStructureException extends RuntimeException {

    private WrongInputStructureException(final String message) {
        super(message);
    }

    public static WrongInputStructureException wrongInputStructureException(final Class<? extends UniversalType> expected,
                                                                            final UniversalType actual,
                                                                            final String location) {
        validateNotNull(expected, "expected");
        validateNotNull(actual, "actual");
        validateNotNull(location, "location");
        final String message = format(
                "Requiring the input to be an '%s' but found '%s' at '%s'",
                describe(expected),
                actual.toNativeJava(),
                location);
        return new WrongInputStructureException(message);
    }
}