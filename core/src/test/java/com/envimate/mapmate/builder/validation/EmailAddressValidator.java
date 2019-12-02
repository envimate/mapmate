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

package com.envimate.mapmate.builder.validation;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static com.envimate.mapmate.builder.validation.CustomTypeValidationException.customTypeValidationException;

public final class EmailAddressValidator {
    private EmailAddressValidator() {
    }

    public static String ensureEmailAddress(final String value, final String description) {
        final String sanitized = SanityValidator.sanitized(value, description);
        try {
            final InternetAddress emailAddress = new InternetAddress(sanitized);
            emailAddress.validate();
            return sanitized.toLowerCase();
        } catch (final AddressException ignored) {
            throw customTypeValidationException("Invalid email address: '%s'", value);
        }
    }
}
