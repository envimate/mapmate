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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.envimate.mapmate.serialization.methods.SerializationMethodNotCompatibleException.serializationMethodNotCompatibleException;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public final class UniqueNoArgumentsMethodReturningStringCPMethod implements SerializationCPMethodDefinition {
    private UniqueNoArgumentsMethodReturningStringCPMethod() {
    }

    public static UniqueNoArgumentsMethodReturningStringCPMethod theUniqueNoArgumentsMethodReturningStringCPMethod() {
        return new UniqueNoArgumentsMethodReturningStringCPMethod();
    }

    @Override
    public SerializationCPMethod verifyCompatibility(final Class<?> targetType) {
        final List<Method> serializerMethodCandidates = Arrays.stream(targetType.getMethods())
                .filter(method -> {
                    final int modifiers = method.getModifiers();
                    return isPublic(modifiers) && !(
                            isStatic(modifiers) || Modifier.isAbstract(modifiers)
                    );
                }).filter(method -> !method.getName().equals("toString"))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> method.getReturnType() == String.class)
                .collect(Collectors.toList());
        if (serializerMethodCandidates.size() == 0) {
            throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "' " +
                    "does not have a no argument method returning String not named toString");
        } else if (serializerMethodCandidates.size() > 1) {
            throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "' " +
                    "does not have a UNIQUE no argument method returning String not named toString. " +
                    "The following methods meet this criteria: " + serializerMethodCandidates);
        } else {
            final Method method = serializerMethodCandidates.get(0);
            return ReflectionMethodSerializationCPMethod.reflectionMethodSerializationCPMethod(method);
        }
    }
}
