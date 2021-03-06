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

package com.envimate.mapmate.deserialization.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.mapmate.deserialization.validation.CustomPrimitiveValidationException.customPrimitiveValidationException;

public final class StringRegexValidator {
    private StringRegexValidator() {
    }

    public static String matchingRegexValue(final String value, final Pattern pattern, final String description) {
        final String sanitized = SecurityValidator.sanitized(value, description);
        final Matcher matcher = pattern.matcher(sanitized);
        if (matcher.matches()) {
            return sanitized;
        } else {
            throw customPrimitiveValidationException("%s does not match regex %s", value, pattern);
        }
    }
}
