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

import com.envimate.mapmate.definitions.hub.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.envimate.mapmate.definitions.DefinitionMultiplexer.multiplex;
import static com.envimate.mapmate.definitions.DefinitionNotFoundException.definitionNotFound;
import static com.envimate.mapmate.definitions.hub.FullType.type;
import static com.envimate.mapmate.deserialization.UnknownReferenceException.fromType;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Definitions {
    private final Map<FullType, Definition> definitions;

    public static Definitions definitions(final Map<FullType, Definition> definitions) {
        return new Definitions(definitions);
    }

    public Definition getDefinitionForType(final FullType targetType) {
        return getOptionalDefinitionForType(targetType)
                .orElseThrow(() -> definitionNotFound(targetType));
    }

    public Optional<Definition> getOptionalDefinitionForType(final FullType targetType) {
        if (!this.definitions.containsKey(targetType)) {
            return Optional.empty();
        }
        return of(this.definitions.get(targetType));
    }

    // TODO call this
    public void validateNoUnsupportedOutgoingReferences() {
        final List<FullType> references = this.allReferences();
        for (final FullType reference : references) {
            if (this.getOptionalDefinitionForType(reference).isEmpty()) {
                throw fromType(reference);
            }
        }
    }

    private List<FullType> allReferences() {
        final List<FullType> allReferences = new LinkedList<>();
        this.definitions.values().forEach(definition -> {
            multiplex(definition).forSerializedObject(serializedObject -> {
                final List<FullType> references = serializedObject.deserializer().fields().referencedTypes();
                allReferences.addAll(references);
            });
        });
        return allReferences;
    }

    // TODO remove
    public int countCustomPrimitives() {
        return (int) this.definitions.values().stream()
                .filter(definition -> definition instanceof CustomPrimitiveDefinition)
                .count();
    }

    // TODO remove
    public int countSerializedObjects() {
        return (int) this.definitions.values().stream()
                .filter(definition -> definition instanceof SerializedObjectDefinition)
                .count();
    }

    public String dump() {
        final StringBuilder stringBuilder = new StringBuilder(10);
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Serialized Objects:\n");
        this.definitions.values().stream()
                .filter(definition -> definition instanceof SerializedObjectDefinition)
                .map(Definition::type)
                .map(FullType::description)
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Custom Primitives:\n");
        this.definitions.values().stream()
                .filter(definition -> definition instanceof CustomPrimitiveDefinition)
                .map(Definition::type)
                .map(FullType::description)
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Collections:\n");
        this.definitions.values().stream()
                .filter(definition -> definition instanceof CollectionDefinition)
                .map(Definition::type)
                .map(FullType::description)
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        return stringBuilder.toString();
    }
}
