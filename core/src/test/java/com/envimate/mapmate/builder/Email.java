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

import com.envimate.mapmate.builder.models.customconvention.Body;
import com.envimate.mapmate.builder.models.customconvention.EmailAddress;
import com.envimate.mapmate.builder.models.customconvention.Subject;
import com.envimate.mapmate.builder.validation.RequiredParameterValidator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Email {
    public final com.envimate.mapmate.builder.models.customconvention.EmailAddress sender;
    public final com.envimate.mapmate.builder.models.customconvention.EmailAddress receiver;
    public final com.envimate.mapmate.builder.models.customconvention.Subject subject;
    public final com.envimate.mapmate.builder.models.customconvention.Body body;

    public static Email restore(final com.envimate.mapmate.builder.models.customconvention.EmailAddress sender,
                                final EmailAddress receiver,
                                final Subject subject,
                                final Body body) {
        RequiredParameterValidator.ensureNotNull(sender, "sender");
        RequiredParameterValidator.ensureNotNull(receiver, "receiver");
        RequiredParameterValidator.ensureNotNull(body, "body");
        return new Email(sender, receiver, subject, body);
    }
}
