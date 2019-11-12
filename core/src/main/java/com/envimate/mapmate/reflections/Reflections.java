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
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.envimate.mapmate.reflections.CodeNeedsToBeCompiledWithParameterNamesException.validateParameterNamesArePresent;
import static com.envimate.mapmate.reflections.FactoryMethodNotFoundException.factoryMethodNotFound;
import static com.envimate.mapmate.reflections.MultipleFactoryMethodsException.multipleFactoryMethodsFound;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class Reflections {

    private Reflections() {
    }

    public static Method findUniqueFactoryMethod(final Class<?> type) {
        final List<Method> factoryMethods = factoryMethods(type)
                .collect(toList());
        return extractSingleFactoryMethod(factoryMethods, type);
    }

    public static Method factoryMethodByName(final Class<?> type, final String name) {
        final List<Method> factoryMethodsByName = findFactoryMethodsByName(type, name);
        return extractSingleFactoryMethod(factoryMethodsByName, type);
    }

    public static List<Method> findFactoryMethodsByName(final Class<?> type, final String name) {
        final List<Method> factoryMethods = factoryMethods(type)
                .filter(method -> method.getName().equals(name))
                .collect(toList());
        return factoryMethods;
    }

    private static Stream<Method> factoryMethods(final Class<?> type) {
        return stream(type.getMethods())
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType() == type);
    }

    public static boolean hasPublicStringMethodWithZeroArgumentsNamed(final Class<?> type, final String methodName) {
        final MethodType methodType = MethodType.methodType(String.class);
        try {
            publicLookup().findVirtual(type, methodName, methodType);
            return true;
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return false;
        }
    }

    public static MethodHandle findPublicStringMethodByName(final Class<?> type, final String methodName) {
        final MethodType methodType = MethodType.methodType(String.class);
        try {
            final MethodHandle methodHandle = publicLookup()
                    .findVirtual(type, methodName, methodType);
            return methodHandle;
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            final String msg = String.format("a public method named '%s' could not be found on class '%s'",
                    methodName,
                    type.getName());
            throw new IllegalArgumentException(msg, e);
        }
    }

    public static Method findFactoryMethodWithClassFieldsAsParameters(final Class<?> type) {
        final Field[] declaredFields = type.getDeclaredFields();
        final Method[] methods = type.getMethods();
        final List<Method> factoryMethods = stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> {
                    final List<Class<?>> parameterTypes = asList(method.getParameterTypes());
                    if (parameterTypes.size() != declaredFields.length) {
                        return false;
                    }
                    if (parameterTypes.contains(String.class)) {
                        return false;
                    }

                    for (final Class<?> parameterType : parameterTypes) {
                        if (!containsRightType(parameterType, declaredFields)) {
                            return false;
                        }
                    }
                    return true;
                }).collect(toList());
        return extractSingleFactoryMethod(factoryMethods, type);
    }

    private static Method extractSingleFactoryMethod(final List<Method> factoryMethods,
                                                     final Class<?> type) {
        final int factoryMethodsFound = factoryMethods.size();
        if (factoryMethodsFound > 1) {
            throw multipleFactoryMethodsFound(type);
        }
        if (factoryMethodsFound == 0) {
            throw factoryMethodNotFound(type);
        }
        final Method method = factoryMethods.get(0);
        validateParameterNamesArePresent(method);
        return method;
    }

    public static boolean isMethodCompatibleWithFields(final Executable method, final Field[] fields) {
        validateParameterNamesArePresent(method);
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (fields.length != parameterTypes.length) {
            return false;
        }

        for (final Field serializedField : fields) {
            final boolean present = stream(parameterTypes).anyMatch(aClass -> serializedField.getType().equals(aClass));
            if (!present) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsRightType(final Class<?> parameterType, final Field[] fields) {
        for (final Field candidate : fields) {
            if (isRightType(candidate, parameterType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRightType(final Field field, final Class<?> parameter) {
        if (isCollection(field)) {
            if (!parameter.isArray()) {
                return false;
            }
            final Class<?> arrayType = parameter.getComponentType();

            final ParameterizedType listParameterizedType = (ParameterizedType) field.getGenericType();
            final Class<?> listType = (Class<?>) listParameterizedType.getActualTypeArguments()[0];

            return arrayType.equals(listType);
        }
        return field.getClass().equals(parameter);
    }

    private static boolean isCollection(final Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }
}
