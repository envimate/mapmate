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

package com.envimate.mapmate.builder.conventional.customprimitives.classannotation;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.builder.definitions.IncompatibleCustomPrimitiveException.incompatibleCustomPrimitiveException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveClassAnnotationFactory implements CustomPrimitiveDefinitionFactory {
    public static CustomPrimitiveClassAnnotationFactory customPrimitiveClassAnnotationFactory() {
        return new CustomPrimitiveClassAnnotationFactory();
    }

    @Override
    public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
        final MapMatePrimitive[] annotationsByType = type.getAnnotationsByType(MapMatePrimitive.class);
        if (annotationsByType.length == 1) {
            final MapMatePrimitive customPrimitiveAnnotation = annotationsByType[0];
            final Method serializationMethod = this.findSerializerMethod(type, customPrimitiveAnnotation);
            final Method deserializationMethod = this.findDeserializerMethod(type, customPrimitiveAnnotation);
            return Optional.of(customPrimitiveDefinition(type, serializationMethod, deserializationMethod));
        }
        return Optional.empty();
    }

    private Method findSerializerMethod(final Class<?> type, final MapMatePrimitive customPrimitiveAnnotation) {
        final String serializationMethodName = customPrimitiveAnnotation.serializationMethodName();
        try {
            return type.getMethod(serializationMethodName);
        } catch (final NoSuchMethodException e) {
            throw incompatibleCustomPrimitiveException(e,
                    "Could not find the serializer method with name %s in type %s mentioned in annotation %s",
                    serializationMethodName,
                    type,
                    customPrimitiveAnnotation
            );
        }
    }

    private Method findDeserializerMethod(final Class<?> type, final MapMatePrimitive customPrimitiveAnnotation) {
        final String deserializationMethodName = customPrimitiveAnnotation.deserializationMethodName();
        try {
            return type.getMethod(deserializationMethodName, String.class);
        } catch (final NoSuchMethodException e) {
            throw incompatibleCustomPrimitiveException(e,
                    "Could not find the deserializer method with name %s in type %s mentioned in annotation %s",
                    deserializationMethodName,
                    type,
                    customPrimitiveAnnotation
            );
        }
    }
}
