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

package com.envimate.mapmate.builder.detection.serializedobject;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.reflections.Reflections.isMethodCompatibleWithFields;
import static java.lang.reflect.Modifier.*;
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

    public static <T extends Executable> Optional<T> findMatchingMethod(final Field[] serializedFields, final List<T> methods) {
        if (serializedFields.length == 0) {
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

    private static boolean isMostLikelyACustomPrimitive(final Field[] fields, final Executable executable) {
        final boolean isMostLikelyACustomPrimitive = fields.length == 0 &&
                executable.getParameterCount() == 1 &&
                executable.getParameterTypes()[0] == String.class;
        return isMostLikelyACustomPrimitive;
    }
}
