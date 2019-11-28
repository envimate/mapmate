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
import com.envimate.mapmate.builder.detection.customprimitive.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.deserialization.deserializers.customprimitives.CustomPrimitiveDeserializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static com.envimate.mapmate.deserialization.deserializers.customprimitives.CustomPrimitiveByConstructorDeserializer.createDeserializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructorBasedCustomPrimitiveDeserializationDetector implements CustomPrimitiveDeserializationDetector {
    private final CustomPrimitiveMappings mappings;

    public static CustomPrimitiveDeserializationDetector constructorBased(final CustomPrimitiveMappings mappings) {
        validateNotNull(mappings, "mappings");
        return new ConstructorBasedCustomPrimitiveDeserializationDetector(mappings);
    }

    @Override
    public Optional<CustomPrimitiveDeserializer> detect(final CachedReflectionType type) {
        return fittingConstructor(type.type())
                .map(constructor -> createDeserializer(type.type(), constructor));
    }

    private Optional<Constructor<?>> fittingConstructor(final Class<?> type) {
        final Constructor<?>[] constructors = type.getConstructors();
        for (final Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() != 1) {
                continue;
            }
            final Class<?> parameterType = constructor.getParameterTypes()[0];
            if (this.mappings.isPrimitiveType(parameterType)) {
                return of(constructor);
            }
        }
        return empty();
    }
}
