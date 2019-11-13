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

package com.envimate.mapmate.builder.detection.serializedobject.detectors;

import com.envimate.mapmate.builder.detection.serializedobject.SerializedObjectDeserializationDetector;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.Common.findMatchingMethod;
import static com.envimate.mapmate.builder.definitions.deserializers.SerializedObjectByConstructorDeserializer.createDeserializer;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructorBasedDeserializationDetector implements SerializedObjectDeserializationDetector {

    public static SerializedObjectDeserializationDetector constructorBased() {
        return new ConstructorBasedDeserializationDetector();
    }

    @Override
    public Optional<DeserializationDTOMethod> detect(final Class<?> type, final Field[] fields) {
        final Constructor<?>[] constructors = type.getConstructors();
        final List<Constructor<?>> deserializerConstructors = stream(constructors)
                .filter(constructor -> isPublic(constructor.getModifiers()))
                .collect(toList());
        return findMatchingMethod(fields, deserializerConstructors)
                .map(constructor -> createDeserializer(type, constructor));
    }
}
