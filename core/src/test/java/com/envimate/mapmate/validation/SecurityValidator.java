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

package com.envimate.mapmate.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.mapmate.validation.CustomPrimitiveValidationException.customPrimitiveValidationException;

final class SecurityValidator {
    private static final int MAX_LENGTH = 1024;
    private static final Pattern MULTIPLE_WHITESPACES = Pattern.compile("\\s{2,}");

    private SecurityValidator() {
    }

    static String sanitized(final String value, final String description) {
        final String sanitized;
        if (value != null) {
            if (value.length() > MAX_LENGTH) {
                throw customPrimitiveValidationException("%s is too long, max %s characters are allowed.",
                        description,
                        MAX_LENGTH);
            }
            final Matcher matcher = MULTIPLE_WHITESPACES.matcher(value);
            final String singleWhiteSpaced;
            if (matcher.matches()) {
                singleWhiteSpaced = matcher.replaceAll(" ");
            } else {
                singleWhiteSpaced = value;
            }
            sanitized = singleWhiteSpaced.trim();
        } else {
            sanitized = null;
        }
        return sanitized;
    }

}
