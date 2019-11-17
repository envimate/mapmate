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

package com.envimate.mapmate.builder.detection.serializedobject.deserialization;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public final class Common {

    private Common() {
    }

    public static List<Method> detectDeserializerMethods(final Class<?> type) {
        final Method[] methods = type.getMethods();
        return stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> method.getParameterCount() > 0)
                .collect(toList());
    }

    public static <T extends Executable> Optional<T> findMatchingMethod(final List<Class<?>> serializedFields, final List<T> methods) {
        if (serializedFields.isEmpty()) {
            return empty();
        }

        T deserializationConstructor = null;
        if (methods.size() == 1) {
            deserializationConstructor = methods.get(0);
        } else {
            final Optional<T> firstCompatibleDeserializerConstructor = methods.stream()
                    .filter(constructor -> isMethodCompatibleWithFields(constructor, serializedFields))
                    .findFirst();
            if (firstCompatibleDeserializerConstructor.isPresent()) {
                deserializationConstructor = firstCompatibleDeserializerConstructor.get();
            }
        }

        if (isMostLikelyACustomPrimitive(serializedFields, deserializationConstructor)) {
            return empty();
        }

        return ofNullable(deserializationConstructor);
    }

    public static boolean isMethodCompatibleWithFields(final Executable method, final List<Class<?>> fields) {
        final List<Class<?>> parameterTypes = asList(method.getParameterTypes());
        if (fields.size() != parameterTypes.size()) {
            return false;
        }

        for (final Class<?> serializedField : fields) {
            final boolean present = parameterTypes.contains(serializedField);
            if (!present) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMostLikelyACustomPrimitive(final List<Class<?>> fields, final Executable executable) {
        final boolean isMostLikelyACustomPrimitive = fields.isEmpty() &&
                executable.getParameterCount() == 1 &&
                executable.getParameterTypes()[0] == String.class;
        return isMostLikelyACustomPrimitive;
    }
}
