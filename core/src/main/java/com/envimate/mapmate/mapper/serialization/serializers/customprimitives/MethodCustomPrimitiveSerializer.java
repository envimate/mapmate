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

package com.envimate.mapmate.mapper.serialization.serializers.customprimitives;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.envimate.mapmate.scanner.builder.detection.customprimitive.IncompatibleCustomPrimitiveException.incompatibleCustomPrimitiveException;
import static com.envimate.mapmate.mapper.serialization.serializers.customprimitives.CustomPrimitiveSerializationMethodCallException.customPrimitiveSerializationMethodCallException;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodCustomPrimitiveSerializer implements CustomPrimitiveSerializer {
    private final Method serializationMethod;

    public static CustomPrimitiveSerializer createSerializer(final Class<?> type,
                                                             final Method serializationMethod) {
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
        return new MethodCustomPrimitiveSerializer(serializationMethod);
    }

    @Override
    public Object serialize(final Object object) {
        try {
            return this.serializationMethod.invoke(object);
        } catch (final IllegalAccessException e) {
            throw customPrimitiveSerializationMethodCallException(format(
                    "This should never happen. Called serialization method %s for custom type %s on instance %s",
                    this.serializationMethod, object.getClass(), object), e);
        } catch (final InvocationTargetException e) {
            throw customPrimitiveSerializationMethodCallException(format(
                    "Got exception calling serialization method %s for custom type %s on instance %s",
                    this.serializationMethod, object.getClass(), object), e);
        }
    }
}
