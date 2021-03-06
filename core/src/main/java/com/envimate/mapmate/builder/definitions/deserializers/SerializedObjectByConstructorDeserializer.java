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

package com.envimate.mapmate.builder.definitions.deserializers;

import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Map;

import static com.envimate.mapmate.builder.definitions.IncompatibleSerializedObjectException.incompatibleSerializedObjectException;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectByConstructorDeserializer implements DeserializationDTOMethod {
    private final Map<String, Class<?>> fields;
    private final Constructor<?> factoryConstructor;
    private final String[] parameterNames;

    public static DeserializationDTOMethod createDeserializer(final Class<?> type,
                                                              final Constructor<?> deserializationConstructor) {
        validateDeserializerModifiers(type, deserializationConstructor);
        return verifiedDeserializationDTOConstructor(deserializationConstructor);
    }

    private static SerializedObjectByConstructorDeserializer verifiedDeserializationDTOConstructor(
            final Constructor<?> factoryConstructor) {
        final Parameter[] parameters = factoryConstructor.getParameters();
        final String[] parameterNames = stream(parameters)
                .map(Parameter::getName)
                .toArray(String[]::new);
        final Map<String, Class<?>> parameterFields = stream(parameters)
                .collect(toMap(Parameter::getName, Parameter::getType));
        return new SerializedObjectByConstructorDeserializer(parameterFields, factoryConstructor, parameterNames);
    }

    @Override
    public Object deserialize(final Class<?> targetType,
                              final Map<String, Object> elements) throws Exception {
        final Object[] arguments = new Object[this.parameterNames.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = elements.get(this.parameterNames[i]);
        }
        return this.factoryConstructor.newInstance(arguments);
    }

    @Override
    public Map<String, Class<?>> elements(final Class<?> targetType) {
        return unmodifiableMap(this.fields);
    }

    private static void validateDeserializerModifiers(final Class<?> type, final Constructor<?> deserializationConstructor) {
        final int deserializationMethodModifiers = deserializationConstructor.getModifiers();

        if (!isPublic(deserializationMethodModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The deserialization constructor %s configured for the SerializedObject of type %s must be public",
                    deserializationConstructor, type);
        }
        if (isAbstract(deserializationMethodModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The deserialization constructor %s configured for the SerializedObject of type %s must not be abstract",
                    deserializationConstructor, type);
        }
        if (deserializationConstructor.getDeclaringClass() != type) {
            throw incompatibleSerializedObjectException(
                    "The deserialization constructor %s configured for the SerializedObject of type %s must return the DTO",
                    deserializationConstructor, type);
        }
    }
}
