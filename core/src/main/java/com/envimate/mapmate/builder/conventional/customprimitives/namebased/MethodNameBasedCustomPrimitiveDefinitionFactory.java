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
import com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveDeserializer;
import com.envimate.mapmate.builder.definitions.serializers.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.untypedCustomPrimitiveDefinition;
import static com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveByConstructorDeserializer.createDeserializer;
import static com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveByMethodDeserializer.createDeserializer;
import static com.envimate.mapmate.builder.definitions.serializers.CustomPrimitiveByMethodSerializer.createSerializer;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodNameBasedCustomPrimitiveDefinitionFactory implements CustomPrimitiveDefinitionFactory {
    private final Pattern serializationMethodName;
    private final Pattern deserializationMethodName;

    public static MethodNameBasedCustomPrimitiveDefinitionFactory nameBasedCustomPrimitiveDefinitionFactory(
            final String serializationMethodName,
            final String deserializationMethodName) {
        return new MethodNameBasedCustomPrimitiveDefinitionFactory(
                compile(serializationMethodName),
                compile(deserializationMethodName));
    }

    @Override
    public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
        final Method[] methods = type.getMethods();
        final Optional<CustomPrimitiveSerializer<?>> serializer = findSerializer(type, methods);
        final Optional<CustomPrimitiveDeserializer<?>> deserializer = findDeserializer(type, methods);

        if (serializer.isPresent() && deserializer.isPresent()) {
            return of(untypedCustomPrimitiveDefinition(type, serializer.get(), deserializer.get()));
        }
        return empty();
    }

    private Optional<CustomPrimitiveSerializer<?>> findSerializer(final Class<?> type, final Method[] methods) {
        return findSerializerMethod(methods, this.serializationMethodName)
                .map(method -> createSerializer(type, method));
    }

    private static Optional<Method> findSerializerMethod(final Method[] methods,
                                                         final Pattern methodNamePattern) {
        return stream(methods)
                .filter(method -> !isStatic(method.getModifiers()))
                .filter(method -> !isAbstract(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(String.class))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> methodNamePattern.matcher(method.getName()).matches())
                .findFirst();
    }

    private Optional<CustomPrimitiveDeserializer<?>> findDeserializer(final Class<?> type,
                                                                      final Method[] methods) {
        return methodDeserializer(type, methods).or(() -> constructorDeserializer(type));
    }

    private Optional<CustomPrimitiveDeserializer<?>> methodDeserializer(final Class<?> type,
                                                                        final Method[] methods) {
        return findDeserializerMethod(type, methods, this.deserializationMethodName)
                .map(method -> createDeserializer(type, method));
    }

    private static Optional<Method> findDeserializerMethod(final Class<?> type,
                                                           final Method[] methods,
                                                           final Pattern methodNamePattern) {
        final List<Method> deserializerMethodCandidates = stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0].equals(String.class))
                .collect(toList());

        final List<Method> methodsMatchingName = deserializerMethodCandidates.stream()
                .filter(method -> methodNamePattern.matcher(method.getName()).matches())
                .collect(toList());
        if (methodsMatchingName.size() > 0) {
            return of(methodsMatchingName.get(0));
        }

        final List<Method> methodsMatchingClassName = deserializerMethodCandidates.stream()
                .filter(method -> method.getName().toLowerCase().contains(type.getSimpleName().toLowerCase()))
                .collect(toList());
        if (methodsMatchingClassName.size() > 0) {
            return of(methodsMatchingClassName.get(0));
        }
        return empty();
    }

    private static Optional<CustomPrimitiveDeserializer<?>> constructorDeserializer(final Class<?> type) {
        return stringConstructor(type)
                .map(constructor -> createDeserializer(type, constructor));
    }

    private static <T> Optional<Constructor<T>> stringConstructor(final Class<T> type) {
        try {
            return of(type.getConstructor(String.class));
        } catch (final NoSuchMethodException e) {
            return empty();
        }
    }
}
