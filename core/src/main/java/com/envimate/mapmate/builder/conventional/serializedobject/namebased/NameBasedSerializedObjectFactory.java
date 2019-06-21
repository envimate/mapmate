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

package com.envimate.mapmate.builder.conventional.serializedobject.namebased;

import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinitionFactory;
import com.envimate.mapmate.reflections.Reflections;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NameBasedSerializedObjectFactory implements SerializedObjectDefinitionFactory {
    private final List<Pattern> patterns;
    private final Pattern deserializationMethodNamePattern;

    public static NameBasedSerializedObjectFactory nameBasedSerializedObjectFactory(
            final List<Pattern> patterns,
            final String deserializationMethodNamePattern) {
        return new NameBasedSerializedObjectFactory(patterns, Pattern.compile(deserializationMethodNamePattern));
    }

    @Override
    public Optional<SerializedObjectDefinition> analyze(final Class<?> type) {
        final String typeName = type.getName();
        final boolean anyMatch = this.patterns.stream().anyMatch(pattern -> pattern.matcher(typeName).matches());
        if (anyMatch) {
            final Field[] serializedFields = this.detectFields(type);
            final List<Method> deserializerCandidates = this.detectDeserializerMethods(type);

            Method deserializerMethod = null;
            if (deserializerCandidates.size() == 1) {
                deserializerMethod = deserializerCandidates.get(0);
            } else if (deserializerCandidates.size() > 1) {
                final Optional<Method> byNamePattern = this.detectByNamePattern(deserializerCandidates, serializedFields);
                if (byNamePattern.isPresent()) {
                    deserializerMethod = byNamePattern.get();
                } else {
                    final String typeSimpleNameLowerCased = type.getSimpleName().toLowerCase();
                    final Optional<Method> byTypeName = deserializerCandidates.stream()
                            .filter(method -> method.getName().toLowerCase().contains(typeSimpleNameLowerCased))
                            .findFirst();
                    if (byTypeName.isPresent()) {
                        deserializerMethod = byTypeName.get();
                    }
                }
            }
            if (serializedFields.length > 0 || deserializerMethod != null) {
                return Optional.of(serializedObjectDefinition(type, serializedFields, deserializerMethod));
            }
        }
        return Optional.empty();
    }

    private Optional<Method> detectByNamePattern(final List<Method> deserializerCandidates,
                                                 final Field[] serializedFields) {
        final List<Method> withMatchingName = deserializerCandidates.stream()
                .filter(method -> this.deserializationMethodNamePattern.matcher(method.getName()).matches())
                .collect(Collectors.toList());
        if (withMatchingName.size() == 1) {
            return Optional.of(deserializerCandidates.get(0));
        } else if (withMatchingName.size() > 1) {
            return deserializerCandidates.stream()
                    .filter(method -> Reflections.isMethodCompatibleWithFields(method, serializedFields))
                    .findFirst();
        }
        return Optional.empty();
    }

    private List<Method> detectDeserializerMethods(final Class<?> type) {
        final Method[] methods = type.getMethods();
        return stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> method.getParameterCount() > 0)
                .collect(Collectors.toList());
    }

    private Field[] detectFields(final Class<?> type) {
        return stream(type.getFields())
                .filter(field -> isPublic(field.getModifiers()))
                .filter(field -> !isStatic(field.getModifiers()))
                .filter(field -> !isTransient(field.getModifiers()))
                .toArray(Field[]::new);
    }
}
