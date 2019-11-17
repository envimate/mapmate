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

package com.envimate.mapmate.builder.detection.serializedobject.deserialization;

import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.IncompatibleSerializedObjectException.incompatibleSerializedObjectException;
import static com.envimate.mapmate.deserialization.deserializers.serializedobjects.MethodSerializedObjectDeserializer.methodDeserializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotationBasedDeserializationDetector implements SerializedObjectDeserializationDetector {
    private final Class<? extends Annotation> annotation;

    public static SerializedObjectDeserializationDetector annotationBasedDeserialzer(
            final Class<? extends Annotation> annotation) {
        validateNotNull(annotation, "annotation");
        return new AnnotationBasedDeserializationDetector(annotation);
    }

    @Override
    public Optional<SerializedObjectDeserializer> detect(final Class<?> type, final SerializationFields fields) {
        final List<Method> annotatedDeserializationMethods = stream(type.getMethods())
                .filter(method -> {
                    final Annotation[] annotations = method.getAnnotationsByType(this.annotation);
                    return annotations.length > 0;
                })
                .collect(toList());

        if (annotatedDeserializationMethods.isEmpty()) {
            return empty();
        }

        final int annotatedMethods = annotatedDeserializationMethods.size();
        if (annotatedMethods > 1) {
            throw incompatibleSerializedObjectException(
                    "The SerializedObject %s has multiple deserialization methods(%s) annotated as " +
                            "MapMateDeserializationMethod",
                    type,
                    annotatedDeserializationMethods
            );
        }
        final Method deserializationMethod = annotatedDeserializationMethods.get(0);
        return Optional.of(methodDeserializer(type, deserializationMethod));
    }
}
