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

package com.envimate.mapmate.reflections;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.envimate.mapmate.reflections.FactoryMethodNotFoundException.factoryMethodNotFound;
import static java.lang.invoke.MethodHandles.publicLookup;

public final class Reflections {

    private Reflections() {
    }

    public static Method findUniqueFactoryMethod(final Class<?> type) {
        final List<Method> factoryMethods = factoryMethods(type)
                .collect(Collectors.toList());

        final int factoryMethodsFound = factoryMethods.size();
        if (factoryMethodsFound > 1) {
            throw MultipleFactoryMethodsException.multipleFactoryMethodsFound(type);
        } else if (factoryMethodsFound == 0) {
            throw factoryMethodNotFound(type);
        } else {
            return factoryMethods.get(0);
        }
    }

    public static Method factoryMethodByName(final Class<?> type, final String name) {
        final List<Method> factoryMethodsByName = findFactoryMethodsByName(type, name);
        final int factoryMethodsFound = factoryMethodsByName.size();
        if (factoryMethodsFound > 1) {
            throw MultipleFactoryMethodsException.multipleFactoryMethodsFound(type);
        } else if (factoryMethodsFound == 0) {
            throw factoryMethodNotFound(type);
        } else {
            return factoryMethodsByName.get(0);
        }
    }

    public static List<Method> findFactoryMethodsByName(final Class<?> type, final String name) {
        final List<Method> factoryMethods = factoryMethods(type)
                .filter(method -> method.getName().equals(name))
                .collect(Collectors.toList());
        return factoryMethods;
    }

    private static Stream<Method> factoryMethods(final Class<?> type) {
        return Arrays.stream(type.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType() == type);
    }

    private static Method findFactoryMethodByName(final Class<?> type, final String methodName) {
        final Collection<Method> factoryMethods = new ArrayList<>(0);
        for (final Method method : type.getMethods()) {
            if (method.getReturnType() == type &&
                    (method.getModifiers() & Modifier.STATIC) != 0 &&
                    method.getName().equals(methodName)) {
                factoryMethods.add(method);
            }
        }

        if (factoryMethods.size() > 1) {
            throw MultipleFactoryMethodsException.multipleFactoryMethodsFound(type, methodName);
        }

        return factoryMethods.stream()
                .findFirst()
                .orElseThrow(() -> factoryMethodNotFound(type, methodName));
    }

    public static boolean hasPublicStringMethodWithZeroArgumentsNamed(final Class<?> type, final String methodName) {
        final MethodType methodType = MethodType.methodType(String.class);
        try {
            publicLookup().findVirtual(type, methodName, methodType);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return false;
        }
    }

    public static MethodHandle findPublicStringMethodByName(final Class<?> type, final String methodName) {
        final MethodType methodType = MethodType.methodType(String.class);
        try {
            final MethodHandle methodHandle = publicLookup()
                    .findVirtual(type, methodName, methodType);
            return methodHandle;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            final String msg = String.format("a public method named '%s' could not be found on class '%s'",
                    methodName,
                    type.getName());
            throw new IllegalArgumentException(msg, e);
        }
    }
}
