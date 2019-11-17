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

package com.envimate.mapmate.builder.recipes.recursive;

import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.SerializedObjectDefinition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScanResultBuilder {
    private final List<CustomPrimitiveDefinition> customPrimitiveDefinitions;
    private final List<SerializedObjectDefinition> serializedObjectDefinitions;

    public static ScanResultBuilder scanResultBuilder() {
        return new ScanResultBuilder(new LinkedList<>(), new LinkedList<>());
    }

    public void addCustomPrimitive(final CustomPrimitiveDefinition customPrimitiveDefinition) {
        validateNotNull(customPrimitiveDefinition, "customPrimitiveDefinition");
        this.customPrimitiveDefinitions.add(customPrimitiveDefinition);
    }

    public void addSerializedObject(final SerializedObjectDefinition serializedObjectDefinition) {
        validateNotNull(serializedObjectDefinition, "serializedObjectDefinition");
        this.serializedObjectDefinitions.add(serializedObjectDefinition);
    }

    public boolean alreadyHas(final Class<?> type) {
        final long customPrimitives = this.customPrimitiveDefinitions.stream()
                .filter(customPrimitiveDefinition -> customPrimitiveDefinition.type().equals(type))
                .count();
        final long serializedObjects = this.serializedObjectDefinitions.stream()
                .filter(serializedObjectDefinition -> serializedObjectDefinition.type().equals(type))
                .count();
        return (customPrimitives + serializedObjects) != 0L;
    }

    public List<CustomPrimitiveDefinition> customPrimitiveDefinitions() {
        return unmodifiableList(this.customPrimitiveDefinitions);
    }

    public List<SerializedObjectDefinition> serializedObjectDefinitions() {
        return unmodifiableList(this.serializedObjectDefinitions);
    }
}
