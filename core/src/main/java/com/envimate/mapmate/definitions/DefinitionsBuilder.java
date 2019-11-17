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

package com.envimate.mapmate.definitions;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.mapmate.definitions.Definitions.definitions;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionsBuilder {
    private static final int INITIAL_CAPACITY = 10000;

    private final Map<Class<?>, CustomPrimitiveDefinition> customPrimitives = new HashMap<>(INITIAL_CAPACITY);
    private final Map<Class<?>, SerializedObjectDefinition> serializedObjects = new HashMap<>(INITIAL_CAPACITY);

    public static DefinitionsBuilder definitionsBuilder() {
        return new DefinitionsBuilder();
    }

    @SuppressWarnings("CastToConcreteClass")
    public void addDefinition(final Definition definition) {
        if (definition.isCustomPrimitive()) {
            this.customPrimitives.put(definition.type(), (CustomPrimitiveDefinition) definition);
        } else {
            this.serializedObjects.put(definition.type(), (SerializedObjectDefinition) definition);
        }
    }

    public boolean notPresent(final Class<?> detectionCandidate) {
        return !this.customPrimitives.containsKey(detectionCandidate) &&
                !this.serializedObjects.containsKey(detectionCandidate);
    }

    public Definitions build() {
        return definitions(this.customPrimitives.values(), this.serializedObjects.values());
    }
}
