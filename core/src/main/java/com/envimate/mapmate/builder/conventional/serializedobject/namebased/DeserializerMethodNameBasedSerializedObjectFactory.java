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
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeserializerMethodNameBasedSerializedObjectFactory implements SerializedObjectDefinitionFactory {
    private final String deserializationMethodName;

    public static DeserializerMethodNameBasedSerializedObjectFactory deserializerMethodNameBasedSerializedObjectFactory(
            final String deserializationMethodName) {
        return new DeserializerMethodNameBasedSerializedObjectFactory(deserializationMethodName);
    }

    @Override
    public Optional<SerializedObjectDefinition> analyze(final Class<?> type) {
        final Method[] methods = type.getMethods();
        final List<Method> deserializerMethods = stream(methods)
                .filter(method -> method.getName().equals(this.deserializationMethodName))
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .collect(Collectors.toList());
        if (!deserializerMethods.isEmpty()) {
            final Field[] serializedFields = stream(type.getFields())
                    .filter(field -> isPublic(field.getModifiers()))
                    .filter(field -> !isStatic(field.getModifiers()))
                    .filter(field -> !isTransient(field.getModifiers()))
                    .toArray(Field[]::new);

            final int deserializerMethodsFound = deserializerMethods.size();
            Method deserializationMethod = null;
            if (deserializerMethodsFound == 1) {
                deserializationMethod = deserializerMethods.get(0);
            } else if (serializedFields.length > 0) {
                final Optional<Method> firstCompatibleDeserializerMethod = deserializerMethods.stream()
                        .filter(method -> Reflections.isMethodCompatibleWithFields(method, serializedFields))
                        .findFirst();
                if (firstCompatibleDeserializerMethod.isPresent()) {
                    deserializationMethod = firstCompatibleDeserializerMethod.get();
                }
            }

            if (serializedFields.length > 0 || deserializationMethod != null) {
                final boolean isMostLikelyACustomPrimitive = serializedFields.length == 0 &&
                        deserializationMethod.getParameterCount() == 1 &&
                        deserializationMethod.getParameterTypes()[0] == String.class;
                if (!isMostLikelyACustomPrimitive) {
                    return Optional.of(serializedObjectDefinition(type, serializedFields, deserializationMethod));
                }
            }
        }
        return Optional.empty();
    }
}
