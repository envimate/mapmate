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

import com.envimate.mapmate.builder.detection.customprimitive.CustomPrimitiveDeserializationDetector;
import com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveDeserializer;
import com.envimate.mapmate.reflections.CachedReflectionType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.definitions.IncompatibleCustomPrimitiveException.incompatibleCustomPrimitiveException;
import static com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveByMethodDeserializer.createDeserializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodAnnotationBasedCustomPrimitiveDeserializationDetector implements CustomPrimitiveDeserializationDetector {
    private final Class<? extends Annotation> annotation;

    public static CustomPrimitiveDeserializationDetector annotationBasedDeserializer(
            final Class<? extends Annotation> annotation) {
        validateNotNull(annotation, "annotation");
        return new MethodAnnotationBasedCustomPrimitiveDeserializationDetector(annotation);
    }

    @Override
    public Optional<CustomPrimitiveDeserializer<?>> detect(final CachedReflectionType type) {
        final Method[] typeMethods = type.methods();
        final List<Method> deserializerMethods = stream(typeMethods)
                .filter(method -> method.getAnnotationsByType(this.annotation).length > 0)
                .collect(toList());

        if (deserializerMethods.isEmpty()) {
            return empty();
        }
        if (deserializerMethods.size() != 1) {
            throw incompatibleCustomPrimitiveException(
                    "When using %s annotation, it needs" +
                            "be used exactly on one method. Found %s annotations on type %s",
                    this.annotation.getName(),
                    deserializerMethods.size(),
                    type
            );
        }
        final Method deserializationMethod = deserializerMethods.get(0);
        return of(createDeserializer(type.type(), deserializationMethod));
    }
}
