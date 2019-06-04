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

import java.lang.reflect.Method;
import java.util.Optional;

import static com.envimate.mapmate.deserialization.methods.DeserializationMethodNotCompatibleException.deserializationMethodNotCompatibleException;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

public final class SingleFactoryMethodCPDeserializationMethod implements DeserializationCPMethod {

    private SingleFactoryMethodCPDeserializationMethod() {
    }

    public static DeserializationCPMethod theSingleFactoryMethodCPDeserializationMethod() {
        return new SingleFactoryMethodCPDeserializationMethod();
    }

    private static Optional<Method> findMethod(final Class<?> type) {
        final Method[] methods = type.getMethods();
        return stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0].equals(String.class))
                .findFirst();
    }

    @Override
    public void verifyCompatibility(final Class<?> targetType) {
        final Optional<Method> method = findMethod(targetType);
        if (!method.isPresent()) {
            throw deserializationMethodNotCompatibleException("class '" + targetType.getName() + "' does not" +
                    " have a static method with a single String argument");

        }
    }

    @Override
    public Object deserialize(final String input, final Class<?> targetType) throws Exception {
        final Method method = findMethod(targetType).get();
        return method.invoke(null, input);
    }
}
