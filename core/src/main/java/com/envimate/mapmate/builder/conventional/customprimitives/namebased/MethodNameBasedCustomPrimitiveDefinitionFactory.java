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

package com.envimate.mapmate.builder.conventional.customprimitives.namebased;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.builder.definitions.IncompatibleCustomPrimitiveException.incompatibleCustomPrimitiveException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodNameBasedCustomPrimitiveDefinitionFactory implements CustomPrimitiveDefinitionFactory {
    private final String serializationMethodName;
    private final String deserializationMethodName;

    public static MethodNameBasedCustomPrimitiveDefinitionFactory nameBasedCustomPrimitiveDefinitionFactory(
            final String serializationMethodName,
            final String deserializationMethodName
    ) {
        return new MethodNameBasedCustomPrimitiveDefinitionFactory(serializationMethodName, deserializationMethodName);
    }

    @Override
    public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
        final Optional<Method> serializationMethod = this.findSerializerMethod(type, this.serializationMethodName);
        final Optional<Method> deserializationMethod = this.findDeserializerMethod(type, this.deserializationMethodName);

        if (serializationMethod.isPresent() || deserializationMethod.isPresent()) {
            if (serializationMethod.isEmpty() || deserializationMethod.isEmpty()) {
                throw incompatibleCustomPrimitiveException(
                        "Both serialization and deserialization methods need to be present. Found %s serializer and %s " +
                                "deserializer on type %s. If this class is not supposed to be handled by MapMate, add" +
                                "it to the blacklist.",
                        serializationMethod.orElse(null),
                        deserializationMethod.orElse(null),
                        type
                );
            }
            return Optional.of(customPrimitiveDefinition(type, serializationMethod.get(), deserializationMethod.get()));
        }
        return Optional.empty();
    }

    private Optional<Method> findSerializerMethod(final Class<?> type, final String methodName) {
        try {
            final Method method = type.getMethod(methodName);
            final int modifiers = method.getModifiers();
            final Class<?> returnType = method.getReturnType();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && returnType.equals(String.class)) {
                return Optional.of(method);
            }
            return Optional.empty();
        } catch (final NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private Optional<Method> findDeserializerMethod(final Class<?> type, final String methodName) {
        try {
            final Method method = type.getMethod(methodName, String.class);
            final int modifiers = method.getModifiers();
            final Class<?> returnType = method.getReturnType();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && returnType.equals(type)) {
                return Optional.of(method);
            }
            return Optional.empty();
        } catch (final NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
