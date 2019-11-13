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

import com.envimate.mapmate.reflections.CachedReflectionType;
import com.envimate.mapmate.builder.detection.customprimitive.CustomPrimitiveDeserializationDetector;
import com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveDeserializer;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveByConstructorDeserializer.createDeserializer;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class ConstructorBasedCustomPrimitiveDeserializationDetector implements CustomPrimitiveDeserializationDetector {

    public static CustomPrimitiveDeserializationDetector constructorBased() {
        return new ConstructorBasedCustomPrimitiveDeserializationDetector();
    }

    @Override
    public Optional<CustomPrimitiveDeserializer<?>> detect(final CachedReflectionType type) {
        return stringConstructor(type.type())
                .map(constructor -> createDeserializer(type.type(), constructor));
    }

    private static <T> Optional<Constructor<T>> stringConstructor(final Class<T> type) {
        try {
            return of(type.getConstructor(String.class));
        } catch (final NoSuchMethodException e) {
            return empty();
        }
    }
}
