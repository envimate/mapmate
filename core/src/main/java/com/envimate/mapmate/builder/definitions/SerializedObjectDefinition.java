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

package com.envimate.mapmate.builder.definitions;

import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;
import com.envimate.mapmate.validators.NotNullValidator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.conventional.serializedobject.VerifiedDeserializationDTOMethod.verifiedDeserializationDTOMethod;
import static com.envimate.mapmate.builder.definitions.IncompatibleSerializedObjectException.incompatibleserializedObjectException;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectDefinition {
    public final Class<?> type;
    public final SerializationDTOMethod serializer;
    public final DeserializationDTOMethod deserializer;

    public static SerializedObjectDefinition serializedObjectDefinition(final Class<?> type,
                                                                        final SerializationDTOMethod serializer,
                                                                        final DeserializationDTOMethod deserializer) {
        NotNullValidator.validateNotNull(type, "type");
        if (serializer == null) {
            NotNullValidator.validateNotNull(deserializer, "deserializer");
        } else if (deserializer == null) {
            NotNullValidator.validateNotNull(serializer, "serializer");
        }
        return new SerializedObjectDefinition(type, serializer, deserializer);
    }

    public static SerializedObjectDefinition serializedObjectDefinition(final Class<?> type,
                                                                        final Field[] serializedFields,
                                                                        final Method deserializationMethod) {
        SerializationDTOMethod serializer = null;
        DeserializationDTOMethod deserializer = null;
        if (serializedFields.length > 0) {
            serializer = createSerializer(type, serializedFields);
        }
        if (deserializationMethod != null) {
            deserializer = createDeserializer(type, deserializationMethod);
        }

        return serializedObjectDefinition(type, serializer, deserializer);
    }

    public static SerializedObjectDefinition serializedObjectDefinition(final Class<?> type,
                                                                        final Field[] serializedFields,
                                                                        final String deserializationMethodName) {
        final Class<?>[] parameterTypes = stream(serializedFields).map(Field::getType).toArray(Class<?>[]::new);
        try {
            return serializedObjectDefinition(type,
                    serializedFields,
                    type.getMethod(deserializationMethodName, parameterTypes)
            );
        } catch (final NoSuchMethodException e) {
            throw incompatibleserializedObjectException(
                    "Could not find method %s with parameters of types %s", deserializationMethodName,
                    stream(parameterTypes).map(Class::getName).collect(Collectors.joining(",")), e);
        }
    }

    private static DeserializationDTOMethod createDeserializer(final Class<?> type,
                                                               final Method deserializationMethod) {
        validateDeserializerModifiers(type, deserializationMethod);
        return verifiedDeserializationDTOMethod(deserializationMethod);
    }

    private static void validateDeserializerModifiers(final Class<?> type, final Method deserializationMethod) {
        final int deserializationMethodModifiers = deserializationMethod.getModifiers();

        if (!Modifier.isPublic(deserializationMethodModifiers)) {
            throw incompatibleserializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must be public",
                    deserializationMethod, type);
        }
        if (!Modifier.isStatic(deserializationMethodModifiers)) {
            throw incompatibleserializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must be static",
                    deserializationMethod, type);
        }
        if (Modifier.isAbstract(deserializationMethodModifiers)) {
            throw incompatibleserializedObjectException(
                    "The deserialization method %s configured for the SerializedObject of type %s must not be abstract",
                    deserializationMethod, type);
        }
        if (deserializationMethod.getReturnType() != type) {
            throw incompatibleserializedObjectException(
                    "The serialization method %s configured for the SerializedObject of type %s must return the DTO",
                    deserializationMethod, type);
        }
    }

    private static SerializationDTOMethod createSerializer(final Class<?> type, final Field[] serializedFields) {
        if (serializedFields.length < 1) {
            throw incompatibleserializedObjectException(
                    "The SerializedObject %s does not have any serialized fields",
                    type
            );
        }

        stream(serializedFields).forEach(field -> validateFieldModifiers(type, field));

        return (object, serializerCallback) -> {
            final Map<String, Object> normalizedChildren = new HashMap<>(serializedFields.length);

            stream(serializedFields)
                    .forEach(field -> {
                        try {
                            final String name = field.getName();
                            final Object value = field.get(object);
                            final Object serializedValue = serializerCallback.apply(value);
                            normalizedChildren.put(name, serializedValue);
                        } catch (final IllegalAccessException e) {
                            throw new UnsupportedOperationException(String.format(
                                    "This should never happen. Tried to access field %s on instance " +
                                            "%s of type %s during SerializedObject serialization",
                                    field,
                                    object,
                                    type), e);
                        }
                    });

            return normalizedChildren;
        };
    }

    private static void validateFieldModifiers(final Class<?> type, final Field field) {
        final int fieldModifiers = field.getModifiers();

        if (!Modifier.isPublic(fieldModifiers)) {
            throw incompatibleserializedObjectException(
                    "The field %s for the SerializedObject of type %s must be public",
                    field, type);
        }
        if (Modifier.isStatic(fieldModifiers)) {
            throw incompatibleserializedObjectException(
                    "The field %s for the SerializedObject of type %s must not be static",
                    field, type);
        }
        if (Modifier.isTransient(fieldModifiers)) {
            throw incompatibleserializedObjectException(
                    "The field %s for the SerializedObject of type %s must not be transient",
                    field, type);
        }
    }

}
