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

package com.envimate.mapmate.builder.definitions.implementations;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.envimate.mapmate.CustomPrimitiveSerializationMethodCallException.customPrimitiveSerializationMethodCallException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveByMethodSerializer implements CustomPrimitiveSerializer<Object> {
    private final Method serializationMethod;

    public static CustomPrimitiveByMethodSerializer verifiedCustomPrimitiveSerializationMethod(
            final Method serializationMethod
    ) {
        return new CustomPrimitiveByMethodSerializer(serializationMethod);
    }

    @Override
    public String serialize(final Object object) {
        try {
            return (String) this.serializationMethod.invoke(object);
        } catch (final IllegalAccessException e) {
            throw customPrimitiveSerializationMethodCallException(String.format(
                    "This should never happen. Called serialization method %s for custom type %s on instance %s",
                    this.serializationMethod,
                    object.getClass(),
                    object), e);
        } catch (final InvocationTargetException e) {
            throw customPrimitiveSerializationMethodCallException(String.format(
                    "Got exception calling serialization method %s for custom type %s on instance %s",
                    this.serializationMethod, object.getClass(), object),
                    e);
        }
    }
}
