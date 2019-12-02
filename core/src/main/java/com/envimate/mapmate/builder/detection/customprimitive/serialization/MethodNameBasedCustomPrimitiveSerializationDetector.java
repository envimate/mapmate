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

package com.envimate.mapmate.builder.detection.customprimitive.serialization;

import com.envimate.mapmate.builder.detection.customprimitive.CachedReflectionType;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.mapper.serialization.serializers.customprimitives.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.envimate.mapmate.mapper.serialization.serializers.customprimitives.MethodCustomPrimitiveSerializer.createSerializer;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;
import static java.util.regex.Pattern.compile;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodNameBasedCustomPrimitiveSerializationDetector implements CustomPrimitiveSerializationDetector {
    private final CustomPrimitiveMappings mappings;
    private final Pattern serializationMethodName;

    public static CustomPrimitiveSerializationDetector methodNameBased(final CustomPrimitiveMappings mappings,
                                                                       final String pattern) {
        validateNotNull(mappings, "mappings");
        validateNotNull(pattern, "pattern");
        return new MethodNameBasedCustomPrimitiveSerializationDetector(mappings, compile(pattern));
    }

    @Override
    public Optional<CustomPrimitiveSerializer> detect(final CachedReflectionType type) {
        return findSerializerMethod(type.methods())
                .map(method -> createSerializer(type.type(), method));
    }

    private Optional<Method> findSerializerMethod(final Method[] methods) {
        return stream(methods)
                .filter(method -> !isStatic(method.getModifiers()))
                .filter(method -> !isAbstract(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> this.mappings.isPrimitiveType(method.getReturnType()))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> this.serializationMethodName.matcher(method.getName()).matches())
                .findFirst();
    }
}
