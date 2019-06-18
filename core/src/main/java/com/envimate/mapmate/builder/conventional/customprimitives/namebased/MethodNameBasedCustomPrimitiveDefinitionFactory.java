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
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodNameBasedCustomPrimitiveDefinitionFactory implements CustomPrimitiveDefinitionFactory {
    private final Pattern serializationMethodName;
    private final Pattern deserializationMethodName;

    public static MethodNameBasedCustomPrimitiveDefinitionFactory nameBasedCustomPrimitiveDefinitionFactory(
            final String serializationMethodName,
            final String deserializationMethodName
    ) {
        return new MethodNameBasedCustomPrimitiveDefinitionFactory(
                Pattern.compile(serializationMethodName),
                Pattern.compile(deserializationMethodName)
        );
    }

    @Override
    public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
        final Method[] methods = type.getMethods();
        final Optional<Method> serializationMethod = this.findSerializerMethod(
                methods, this.serializationMethodName
        );
        final Optional<Method> deserializationMethod = this.findDeserializerMethod(
                type, methods, this.deserializationMethodName
        );

        if (serializationMethod.isPresent() || deserializationMethod.isPresent()) {
            if (serializationMethod.isPresent() && deserializationMethod.isPresent()) {
                return Optional.of(
                        customPrimitiveDefinition(type, serializationMethod.get(), deserializationMethod.get())
                );
            }

        }
        return Optional.empty();
    }

    private Optional<Method> findSerializerMethod(final Method[] methods,
                                                  final Pattern methodNamePattern) {
        final List<Method> serializerMethodCandidates = stream(methods)
                .filter(method -> !isStatic(method.getModifiers()))
                .filter(method -> !isAbstract(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(String.class))
                .filter(method -> method.getParameterCount() == 0)
                .collect(Collectors.toList());
        final List<Method> methodsMatchingName = serializerMethodCandidates.stream()
                .filter(method -> methodNamePattern.matcher(method.getName()).matches())
                .collect(Collectors.toList());
        if (methodsMatchingName.size() > 0) {
            return Optional.of(methodsMatchingName.get(0));
        }
        return Optional.empty();

    }

    private Optional<Method> findDeserializerMethod(final Class<?> type, final Method[] methods,
                                                    final Pattern methodNamePattern) {
        final List<Method> deserializerMethodCandidates = stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0].equals(String.class))
                .collect(Collectors.toList());
        final List<Method> methodsMatchingName = deserializerMethodCandidates.stream()
                .filter(method -> methodNamePattern.matcher(method.getName()).matches())
                .collect(Collectors.toList());
        if (methodsMatchingName.size() > 0) {
            return Optional.of(methodsMatchingName.get(0));
        }

        final List<Method> methodsMatchingClassName = deserializerMethodCandidates.stream()
                .filter(method -> method.getName().toLowerCase().contains(type.getSimpleName().toLowerCase()))
                .collect(Collectors.toList());
        if (methodsMatchingClassName.size() > 0) {
            return Optional.of(methodsMatchingClassName.get(0));
        }
        return Optional.empty();
    }
}
