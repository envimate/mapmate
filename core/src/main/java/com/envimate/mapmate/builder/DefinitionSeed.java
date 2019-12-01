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

import com.envimate.mapmate.definitions.types.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.mapmate.builder.RequiredCapabilities.none;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionSeed {
    private static final int INITIAL_CAPACITY = 50;
    private final FullType fullType;
    private final Map<SeedReason, RequiredCapabilities> requiredCapabilities;

    public static DefinitionSeed definitionSeed(final FullType fullType) {
        validateNotNull(fullType, "fullType");
        return new DefinitionSeed(fullType, new HashMap<>(INITIAL_CAPACITY));
    }

    public void addRequirements(final SeedReason reason, final RequiredCapabilities requiredCapabilities) {
        this.requiredCapabilities.put(reason, requiredCapabilities);
    }

    public FullType fullType() {
        return this.fullType;
    }

    public RequiredCapabilities requiredCapabilities() {
        final RequiredCapabilities requiredCapabilities = none();
        this.requiredCapabilities.values().forEach(requiredCapabilities::add);
        return requiredCapabilities;
    }
}
