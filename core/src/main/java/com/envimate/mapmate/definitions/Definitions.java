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

import com.envimate.mapmate.builder.DefinitionSeed;
import com.envimate.mapmate.builder.DefinitionSeeds;
import com.envimate.mapmate.builder.RequiredCapabilities;
import com.envimate.mapmate.definitions.types.ClassType;
import com.envimate.mapmate.definitions.types.ResolvedType;
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
import static java.lang.String.format;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Definitions {
    private final Map<ResolvedType, Definition> definitions;

    public static Definitions definitions(final Map<ResolvedType, Definition> definitions, final DefinitionSeeds seeds) {
        final Definitions definitionsObject = new Definitions(definitions);
        definitionsObject.validateNoUnsupportedOutgoingReferences(seeds);
        return definitionsObject;
    }

    public Definition getDefinitionForType(final ResolvedType targetType) {
        return getOptionalDefinitionForType(targetType)
                .orElseThrow(() -> definitionNotFound(targetType, dump()));
    }

    public Optional<Definition> getOptionalDefinitionForType(final ResolvedType targetType) {
        if (!this.definitions.containsKey(targetType)) {
            return Optional.empty();
        }
        return of(this.definitions.get(targetType));
    }

    public void validateNoUnsupportedOutgoingReferences(final DefinitionSeeds seeds) {
        for (final ResolvedType type : seeds.types()) {
            final RequiredCapabilities capabilities = seeds.forType(type).requiredCapabilities();
            if (capabilities.hasDeserialization()) {
                validateDeserialization(type, type, new LinkedList<>());
            }
            if (capabilities.hasSerialization()) {
                validateSerialization(type, type, new LinkedList<>());
            }
        }
    }

    private void validateDeserialization(final ResolvedType candidate, final ResolvedType reason, final List<ResolvedType> alreadyVisited) {
        if (alreadyVisited.contains(candidate)) {
            return;
        }
        alreadyVisited.add(candidate);
        final Definition definition = getOptionalDefinitionForType(candidate).orElseThrow(() ->
                new UnsupportedOperationException(
                        format("Type '%s' is not registered but needs to be in order to support deserialization of '%s'",
                                candidate.description(), reason.description())));
        multiplex(definition)
                .forCustomPrimitive(customPrimitive -> {
                    if (customPrimitive.deserializer().isEmpty()) {
                        throw new UnsupportedOperationException(
                                format("Custom primitive '%s' is not deserializable but needs to be in order to support deserialization of '%s'. %s",
                                        candidate.description(), reason.description(), definition.context()));
                    }
                })
                .forSerializedObject(serializedObject -> {
                    if (serializedObject.deserializer().isEmpty()) {
                        throw new UnsupportedOperationException(
                                format("Serialized object '%s' is not deserializable but needs to be in order to support deserialization of '%s'. %s",
                                        candidate.description(), reason.description(), serializedObject.context()));
                    }
                    serializedObject.deserializer().orElseThrow()
                            .fields()
                            .referencedTypes()
                            .forEach(reference -> validateDeserialization(reference, reason, alreadyVisited));
                })
                .forCollection(collection -> validateDeserialization(collection.contentType(), reason, alreadyVisited));
    }

    private void validateSerialization(final ResolvedType candidate, final ResolvedType reason, final List<ResolvedType> alreadyVisited) {
        if (alreadyVisited.contains(candidate)) {
            return;
        }
        alreadyVisited.add(candidate);
        final Definition definition = getOptionalDefinitionForType(candidate).orElseThrow(() ->
                new UnsupportedOperationException(
                        format("Type '%s' is not registered but needs to be in order to support serialization of '%s'",
                                candidate.description(), reason.description())));
        multiplex(definition)
                .forCustomPrimitive(customPrimitive -> {
                    if (customPrimitive.serializer().isEmpty()) {
                        throw new UnsupportedOperationException(
                                format("Custom primitive '%s' is not serializable but needs to be in order to support serialization of '%s'",
                                        candidate.description(), reason.description()));
                    }
                })
                .forSerializedObject(serializedObject -> {
                    if (serializedObject.serializer().isEmpty()) {
                        throw new UnsupportedOperationException(
                                format("Serialized object '%s' is not serializable but needs to be in order to support serialization of '%s'",
                                        candidate.description(), reason.description()));
                    }
                    serializedObject.serializer().orElseThrow()
                            .fields()
                            .typesList()
                            .forEach(reference -> validateSerialization(reference, reason, alreadyVisited));
                })
                .forCollection(collection -> validateSerialization(collection.contentType(), reason, alreadyVisited));
    }

    public int countCustomPrimitives() {
        return (int) this.definitions.values().stream()
                .filter(definition -> definition instanceof CustomPrimitiveDefinition)
                .count();
    }

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
                .map(ResolvedType::description)
                .sorted()
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Custom Primitives:\n");
        this.definitions.values().stream()
                .filter(definition -> definition instanceof CustomPrimitiveDefinition)
                .map(Definition::type)
                .map(ResolvedType::description)
                .sorted()
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        stringBuilder.append("Collections:\n");
        this.definitions.values().stream()
                .filter(definition -> definition instanceof CollectionDefinition)
                .map(Definition::type)
                .map(ResolvedType::description)
                .sorted()
                .forEach(type -> stringBuilder.append(type).append("\n"));
        stringBuilder.append("------------------------------\n");
        return stringBuilder.toString();
    }
}
