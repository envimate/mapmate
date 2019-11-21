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

package com.envimate.mapmate.builder.detection.customprimitive.deserialization;

import com.envimate.mapmate.builder.detection.customprimitive.CachedReflectionType;
import com.envimate.mapmate.deserialization.deserializers.customprimitives.CustomPrimitiveDeserializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.envimate.mapmate.deserialization.deserializers.customprimitives.CustomPrimitiveByMethodDeserializer.createDeserializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StaticMethodBasedCustomPrimitiveDeserializationDetector implements CustomPrimitiveDeserializationDetector {
    private static final Pattern MATCH_ALL = compile(".*");

    private final Pattern deserializationMethodName;

    public static CustomPrimitiveDeserializationDetector staticMethodBased() {
        return new StaticMethodBasedCustomPrimitiveDeserializationDetector(MATCH_ALL);
    }

    public static CustomPrimitiveDeserializationDetector staticMethodBased(final String pattern) {
        validateNotNull(pattern, "pattern");
        return new StaticMethodBasedCustomPrimitiveDeserializationDetector(compile(pattern));
    }

    @Override
    public Optional<CustomPrimitiveDeserializer> detect(final CachedReflectionType type) {
        return findDeserializerMethod(type, this.deserializationMethodName)
                .map(method -> createDeserializer(type.type(), method));
    }

    private static Optional<Method> findDeserializerMethod(final CachedReflectionType type,
                                                           final Pattern methodNamePattern) {
        final List<Method> deserializerMethodCandidates = stream(type.methods())
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type.type()))
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
                .filter(method -> method.getName().toLowerCase().contains(type.type().getSimpleName().toLowerCase()))
                .collect(toList());
        if (methodsMatchingClassName.size() > 0) {
            return of(methodsMatchingClassName.get(0));
        }
        return empty();
    }

}
