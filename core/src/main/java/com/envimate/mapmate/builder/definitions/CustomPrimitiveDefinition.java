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

import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;
import com.envimate.mapmate.validators.NotNullValidator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.envimate.mapmate.builder.conventional.customprimitives.VerifiedCustomPrimitiveDeserializationMethod.verifiedCustomPrimitiveDeserializationMethod;
import static com.envimate.mapmate.builder.conventional.customprimitives.VerifiedCustomPrimitiveSerializationMethod.verifiedCustomPrimitiveSerializationMethod;
import static com.envimate.mapmate.builder.definitions.IncompatibleCustomPrimitiveException.incompatibleCustomPrimitiveException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveDefinition {
    public final Class<?> type;
    public final SerializationCPMethod serializer;
    public final DeserializationCPMethod deserializer;

    public static CustomPrimitiveDefinition customPrimitiveDefinition(final Class<?> type,
                                                                      final SerializationCPMethod serializer,
                                                                      final DeserializationCPMethod deserializer) {
        NotNullValidator.validateNotNull(type, "type");
        NotNullValidator.validateNotNull(serializer, "serializer");
        NotNullValidator.validateNotNull(deserializer, "deserializer");
        return new CustomPrimitiveDefinition(type, serializer, deserializer);
    }

    public static CustomPrimitiveDefinition customPrimitiveDefinition(final Class<?> type,
                                                                      final Method serializationMethod,
                                                                      final Method deserializationMethod) {
        final SerializationCPMethod serializer = createSerializer(type, serializationMethod);
        final DeserializationCPMethod deserializer = createDeserializer(type, deserializationMethod);
        return customPrimitiveDefinition(type, serializer, deserializer);
    }

    private static SerializationCPMethod createSerializer(final Class<?> type, final Method serializationMethod) {
        final int serializationMethodModifiers = serializationMethod.getModifiers();
        if (!Modifier.isPublic(serializationMethodModifiers)) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must be public",
                    serializationMethod,
                    type
            );
        }
        if (Modifier.isStatic(serializationMethodModifiers)) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must not be static",
                    serializationMethod,
                    type
            );
        }
        if (Modifier.isAbstract(serializationMethodModifiers)) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must not be abstract",
                    serializationMethod,
                    type
            );
        }
        if (serializationMethod.getParameterCount() > 0) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must " +
                            "not accept any parameters",
                    serializationMethod,
                    type
            );
        }
        if (serializationMethod.getReturnType() != String.class) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must return a String",
                    serializationMethod,
                    type
            );
        }
        return verifiedCustomPrimitiveSerializationMethod(serializationMethod);
    }

    private static DeserializationCPMethod createDeserializer(final Class<?> type, final Method deserializationMethod) {
        final int deserializationMethodModifiers = deserializationMethod.getModifiers();
        if (!Modifier.isPublic(deserializationMethodModifiers)) {
            throw incompatibleCustomPrimitiveException(
                    "The deserialization method %s configured for the custom primitive of type %s must be public",
                    deserializationMethod, type);
        }
        if (!Modifier.isStatic(deserializationMethodModifiers)) {
            throw incompatibleCustomPrimitiveException(
                    "The deserialization method %s configured for the custom primitive of type %s must be static",
                    deserializationMethod, type);
        }
        if (Modifier.isAbstract(deserializationMethodModifiers)) {
            throw incompatibleCustomPrimitiveException(
                    "The deserialization method %s configured for the custom primitive of type %s must not be abstract",
                    deserializationMethod, type);
        }
        final Class<?>[] deserializationMethodParameterTypes = deserializationMethod.getParameterTypes();
        if (deserializationMethodParameterTypes.length != 1 ||
                !deserializationMethodParameterTypes[0].equals(String.class)) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must " +
                            "accept only one parameter of type String",
                    deserializationMethod, type);
        }
        if (deserializationMethod.getReturnType() != type) {
            throw incompatibleCustomPrimitiveException(
                    "The serialization method %s configured for the custom primitive of type %s must return " +
                            "the custom primitive", deserializationMethod, type);
        }

        return verifiedCustomPrimitiveDeserializationMethod(deserializationMethod);
    }

}
