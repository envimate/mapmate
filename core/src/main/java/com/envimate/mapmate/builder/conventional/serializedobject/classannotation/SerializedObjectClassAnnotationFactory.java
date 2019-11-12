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

package com.envimate.mapmate.builder.conventional.serializedobject.classannotation;

import com.envimate.mapmate.builder.definitions.IncompatibleSerializedObjectException;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.definitions.SerializedObjectDefinition.serializedObjectDefinition;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectClassAnnotationFactory implements SerializedObjectDefinitionFactory {
    public static SerializedObjectClassAnnotationFactory serializedObjectClassAnnotationFactory() {
        return new SerializedObjectClassAnnotationFactory();
    }

    @Override
    public Optional<SerializedObjectDefinition> analyze(final Class<?> type) {
        final Field[] serializedFields = this.findSerializedFields(type);

        final List<Method> annotatedDeserializationMethods = Arrays.stream(type.getMethods()).filter(method -> {
            final MapMateDeserializationMethod[] annotationsByType = method
                    .getAnnotationsByType(MapMateDeserializationMethod.class);
            return annotationsByType.length > 0;
        }).collect(Collectors.toList());

        Method deserializationMethod = null;

        final int annotatedMethods = annotatedDeserializationMethods.size();
        if (annotatedMethods > 1) {
            throw IncompatibleSerializedObjectException.incompatibleSerializedObjectException(
                    "The SerializedObject %s has multiple deserialization methods(%s) annotated as " +
                            "MapMateDeserializationMethod",
                    type,
                    annotatedDeserializationMethods
            );
        } else if (annotatedMethods == 1) {
            deserializationMethod = annotatedDeserializationMethods.get(0);
        }

        if (serializedFields.length > 0 || deserializationMethod != null) {
            return Optional.of(serializedObjectDefinition(type, serializedFields, deserializationMethod));
        } else {
            return Optional.empty();
        }
    }

    private Field[] findSerializedFields(final Class<?> type) {
        return Arrays.stream(type.getFields())
                .filter(field -> field.isAnnotationPresent(MapMateSerializedField.class))
                .toArray(Field[]::new);
    }
}
