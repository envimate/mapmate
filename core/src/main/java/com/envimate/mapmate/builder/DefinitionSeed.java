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

import com.envimate.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.builder.RequiredCapabilities.none;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Collections.singletonList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionSeed {
    private static final int INITIAL_CAPACITY = 5;

    private final ResolvedType type;
    private final List<String> log = new LinkedList<>();
    private final List<RequiredCapabilities> capabilities;

    public static DefinitionSeed definitionSeed(final ResolvedType fullType) {
        validateNotNull(fullType, "fullType");
        return new DefinitionSeed(fullType, new LinkedList<>());
    }

    public void log(final String message) {
        this.log.add(message);
    }

    public DefinitionSeed childForType(final ResolvedType childType) {
        return new DefinitionSeed(childType, singletonList(requiredCapabilities()));
    }

    public void merge(final DefinitionSeed other) {
        if (!other.type.equals(this.type)) {
            throw new IllegalArgumentException("Cannot merge with different type");
        }
        this.capabilities.addAll(other.capabilities);
    }

    public DefinitionSeed withCapability(final RequiredCapabilities capability) {
        this.capabilities.add(capability);
        return this;
    }

    public ResolvedType type() {
        return this.type;
    }

    public RequiredCapabilities requiredCapabilities() {
        final RequiredCapabilities requiredCapabilities = none();
        this.capabilities.forEach(requiredCapabilities::add);
        return requiredCapabilities;
    }
}
