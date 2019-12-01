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

import com.envimate.mapmate.definitions.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.mapmate.builder.MultiMap.multiMap;
import static com.envimate.mapmate.builder.RequiredCapabilities.none;
import static com.envimate.mapmate.builder.SeedReason.becauseChildOf;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionSeed {
    private static final int INITIAL_CAPACITY = 5;

    private final ResolvedType type;
    private final MultiMap<RequiredCapabilities, SeedReason> capabilities;

    public static DefinitionSeed definitionSeed(final ResolvedType fullType) {
        validateNotNull(fullType, "fullType");
        return new DefinitionSeed(fullType, multiMap(INITIAL_CAPACITY));
    }

    public DefinitionSeed childForType(final ResolvedType childType) {
        return definitionSeed(childType).withCapability(requiredCapabilities(), becauseChildOf(this));
    }

    public void merge(final DefinitionSeed other) {
        if (!other.type.equals(this.type)) {
            throw new IllegalArgumentException("Cannot merge with different type");
        }
        this.capabilities.putAll(other.capabilities);
    }

    public DefinitionSeed withCapability(final RequiredCapabilities capability, final SeedReason reason) {
        this.capabilities.put(capability, reason);
        return this;
    }

    public ResolvedType type() {
        return this.type;
    }

    public RequiredCapabilities requiredCapabilities() {
        final RequiredCapabilities requiredCapabilities = none();
        this.capabilities.keys().forEach(requiredCapabilities::add);
        return requiredCapabilities;
    }
}
