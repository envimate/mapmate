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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.definitions.DefinitionNotFoundException.definitionNotFound;
import static com.envimate.mapmate.deserialization.UnknownReferenceException.fromType;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Definitions {
    private final Collection<CustomPrimitiveDefinition> customPrimitives;
    private final Collection<SerializedObjectDefinition> serializedObjects;

    public static Definitions definitions(final Collection<CustomPrimitiveDefinition> customPrimitives,
                                          final Collection<SerializedObjectDefinition> serializedObjects) {
        return new Definitions(customPrimitives, serializedObjects);
    }

    public Definition getDefinitionForObject(final Object object) {
        final Class<?> targetType = object.getClass();
        return this.getDefinitionForType(targetType);
    }

    public Definition getDefinitionForType(final Class<?> targetType) {
        final List<Definition> allDefinitions = new LinkedList<>();
        allDefinitions.addAll(this.customPrimitives);
        allDefinitions.addAll(this.serializedObjects);
        return allDefinitions.stream()
                .filter(e -> e.type() == targetType)
                .findAny()
                .orElseThrow(() -> definitionNotFound(targetType));
    }

    public Optional<Definition> getOptionalDefinitionForType(final Class<?> targetType) {
        final List<Definition> allDefinitions = new LinkedList<>();
        allDefinitions.addAll(this.customPrimitives);
        allDefinitions.addAll(this.serializedObjects);
        return allDefinitions.stream()
                .filter(e -> e.type() == targetType)
                .findAny();
    }

    public void validateNoUnsupportedOutgoingReferences() {
        final List<Class<?>> references = this.allReferences();
        for (final Class<?> reference : references) {
            if (this.getOptionalDefinitionForType(reference).isEmpty()) {
                throw fromType(reference);
            }
        }
    }

    private List<Class<?>> allReferences() {
        final List<Class<?>> allReferences = new LinkedList<>();
        for (final SerializedObjectDefinition serializedObjectDefinition : this.serializedObjects) {
            final List<Class<?>> references = serializedObjectDefinition.deserializer().fields().referencedTypes();
            allReferences.addAll(references);
        }
        return allReferences;
    }

    public int countCustomPrimitives() {
        return this.customPrimitives.size();
    }

    public int countSerializedObjects() {
        return this.serializedObjects.size();
    }

    public String dump() {
        final StringBuilder stringBuilder = new StringBuilder(10);
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Serialized Objects:\n");
        this.serializedObjects.stream()
                .map(SerializedObjectDefinition::type)
                .map(Class::getName)
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Custom Primitives:\n");
        this.customPrimitives.stream()
                .map(CustomPrimitiveDefinition::type)
                .map(Class::getName)
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        return stringBuilder.toString();
    }
}
