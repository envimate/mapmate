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

package com.envimate.mapmate.deserialization.deserializers.serializedobjects;

import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.deserialization.DeserializationFields;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.detection.serializedobject.IncompatibleSerializedObjectException.incompatibleSerializedObjectException;
import static com.envimate.mapmate.deserialization.DeserializationFields.deserializationFields;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodSerializedObjectDeserializer implements SerializedObjectDeserializer {
    private final DeserializationFields fields;
    private final Method factoryMethod;
    private final String[] parameterNames;

    public static SerializedObjectDeserializer methodNameDeserializer(final FullType type,
                                                                      final String methodName,
                                                                      final Field[] requiredFields) {
        final Class<?>[] parameterTypes = stream(requiredFields).map(Field::getType).toArray(Class<?>[]::new);
        try {
            final Method method = type.type().getMethod(methodName, parameterTypes);
            return methodDeserializer(type, method);
        } catch (final NoSuchMethodException e) {
            throw incompatibleSerializedObjectException(
                    "Could not find method %s with parameters of types %s", methodName,
                    stream(parameterTypes).map(Class::getName).collect(Collectors.joining(",")), e);
        }

    }

    public static SerializedObjectDeserializer methodDeserializer(final FullType type,
                                                                  final Method deserializationMethod) {
        validateDeserializerModifiers(type, deserializationMethod);
        return verifiedDeserializationDTOMethod(deserializationMethod);
    }

    private static MethodSerializedObjectDeserializer verifiedDeserializationDTOMethod(final Method factoryMethod) {
        final Parameter[] parameters = factoryMethod.getParameters();
        final String[] parameterNames = stream(parameters).map(Parameter::getName).toArray(String[]::new);
        final Map<String, FullType> parameterFields = stream(parameters)
                .collect(Collectors.toMap(
                        Parameter::getName,
                        FullType::typeOfParameter
                ));
        return new MethodSerializedObjectDeserializer(deserializationFields(parameterFields), factoryMethod, parameterNames);
    }

    @Override
    public Object deserialize(final Map<String, Object> elements) throws Exception {
        final Object[] arguments = new Object[this.parameterNames.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = elements.get(this.parameterNames[i]);
        }
        return this.factoryMethod.invoke(null, arguments);
    }

    @Override
    public DeserializationFields fields() {
        return this.fields;
    }

    private static void validateDeserializerModifiers(final FullType type, final Method deserializationMethod) {
        final int deserializationMethodModifiers = deserializationMethod.getModifiers();

        if (!isPublic(deserializationMethodModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must be public",
                    deserializationMethod, type);
        }
        if (!isStatic(deserializationMethodModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must be static",
                    deserializationMethod, type);
        }
        if (isAbstract(deserializationMethodModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must not be abstract",
                    deserializationMethod, type);
        }
        if (deserializationMethod.getReturnType() != type.type()) {
            throw incompatibleSerializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must return the DTO",
                    deserializationMethod, type);
        }
    }
}
