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

package com.envimate.mapmate.serialization.methods;

import com.envimate.mapmate.CustomPrimitiveSerializationMethodCallException;

import java.lang.reflect.Method;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public final class ReflectionMethodSerializationCPMethod implements SerializationCPMethod {
    private final Method method;

    private ReflectionMethodSerializationCPMethod(final Method method) {
        validateNotNull(method, "method");
        this.method = method;
    }

    public static ReflectionMethodSerializationCPMethod reflectionMethodSerializationCPMethod(
            final Method method
    ) {
        return new ReflectionMethodSerializationCPMethod(method);
    }

    @Override
    public String serialize(final Object object) {
        try {
            final String ret = (String) this.method.invoke(object);
            return ret;
        } catch (final IllegalAccessException | ClassCastException e) {
            throw CustomPrimitiveSerializationMethodCallException.fromThrowable(
                    "this should never happen, contact the developers.",
                    e,
                    this.method.getDeclaringClass(),
                    this.method,
                    object);
        } catch (final Throwable throwable) {
            throw CustomPrimitiveSerializationMethodCallException.fromThrowable(
                    "exception invoking serialization method",
                    throwable,
                    this.method.getDeclaringClass(),
                    this.method,
                    object);
        }
    }
}
