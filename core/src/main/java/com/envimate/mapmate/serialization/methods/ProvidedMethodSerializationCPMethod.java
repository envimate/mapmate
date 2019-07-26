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

import java.lang.reflect.Method;
import java.util.function.Function;

import static com.envimate.mapmate.serialization.methods.SerializationMethodNotCompatibleException.serializationMethodNotCompatibleException;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public final class ProvidedMethodSerializationCPMethod<T> implements SerializationCPMethod,
        SerializationCPMethodDefinition {
    private final Class<T> type;
    private final Function<T, String> method;

    private ProvidedMethodSerializationCPMethod(final Class<T> type, final Function<T, String> method) {
        validateNotNull(type, "type");
        validateNotNull(method, "method");
        this.type = type;
        this.method = method;
    }

    public static <T> ProvidedMethodSerializationCPMethod<T> providedMethodSerializationCPMethod(
            final Class<T> type, final Function<T, String> method
    ) {
        return new ProvidedMethodSerializationCPMethod<>(type, method);
    }

    @Override
    public SerializationCPMethod verifyCompatibility(final Class<?> targetType) {
        final Class<?> supplierClass = this.method.getClass();
        for (final Method method : supplierClass.getMethods()) {
            if (!method.getName().equals("apply")) {
                continue;
            }
            final Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType.isAssignableFrom(targetType)) {
                return this;
            } else {
                throw serializationMethodNotCompatibleException("Provided method returns object " +
                        "of type '" + parameterType.getSimpleName() + "' but" +
                        " '" + targetType.getSimpleName() + "' is needed.");
            }
        }
        throw new RuntimeException("This should never happen.");
    }

    @Override
    public String serialize(final Object object) {
        return this.method.apply(this.type.cast(object));
    }
}
