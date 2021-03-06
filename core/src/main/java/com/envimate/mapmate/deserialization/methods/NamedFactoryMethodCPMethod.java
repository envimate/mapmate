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

package com.envimate.mapmate.deserialization.methods;

import com.envimate.mapmate.reflections.MethodName;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;

import static com.envimate.mapmate.deserialization.methods.DeserializationMethodNotCompatibleException.deserializationMethodNotCompatibleException;
import static com.envimate.mapmate.reflections.MethodName.fromString;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamedFactoryMethodCPMethod implements DeserializationCPMethod {

    private final MethodName methodName;

    public static DeserializationCPMethod theNamedFactoryMethodCPMethod(final String name) {
        final MethodName methodName = fromString(name);
        return new NamedFactoryMethodCPMethod(methodName);
    }

    @Override
    public void verifyCompatibility(final Class<?> targetType) {
        try {
            targetType.getMethod(this.methodName.internalValueForMapping(), String.class);
        } catch (final NoSuchMethodException e) {
            throw deserializationMethodNotCompatibleException(
                    "class '" + targetType.getName() + "' does not have a static method" +
                            " with a single String argument" +
                            " named '" + this.methodName.internalValueForMapping() + "'", e);
        }
    }

    @Override
    public Object deserialize(final String input, final Class<?> targetType) throws Exception {
        validateNotNull(input, "originalInput");
        final Method method = targetType.getMethod(this.methodName.internalValueForMapping(), String.class);
        return method.invoke(null, input);
    }
}
