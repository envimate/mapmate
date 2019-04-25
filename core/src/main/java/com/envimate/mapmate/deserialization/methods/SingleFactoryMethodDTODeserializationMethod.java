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

import com.envimate.mapmate.reflections.FactoryMethodNotFoundException;
import com.envimate.mapmate.reflections.MultipleFactoryMethodsException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SingleFactoryMethodDTODeserializationMethod implements DeserializationDTOMethod {

    private SingleFactoryMethodDTODeserializationMethod() {
    }

    public static DeserializationDTOMethod singleFactoryMethodDTODeserializationDTOMethod() {
        return new SingleFactoryMethodDTODeserializationMethod();
    }

    @Override
    public Object deserialize(final Class<?> targetType,
                              final Map<String, Object> elements) throws Exception {
        final Method factoryMethod = findFactoryMethod(targetType);
        final Parameter[] parameters = factoryMethod.getParameters();
        final Object[] arguments = new Object[parameters.length];
        for(int i = 0; i < parameters.length; ++i) {
            final String name = parameters[i].getName();
            final Object argument = elements.get(name);
            arguments[i] = argument;
        }
        return factoryMethod.invoke(null, arguments);
    }

    @Override
    public Map<String, Class<?>> elements(final Class<?> targetType) {
        final Method factoryMethod = findFactoryMethod(targetType);
        final Parameter[] parameters = factoryMethod.getParameters();
        final Map<String, Class<?>> references = new HashMap<>();
        for(final Parameter parameter : parameters) {
            final String name = parameter.getName();
            final Class<?> type = parameter.getType();
            references.put(name, type);
        }
        return references;
    }

    private static Method findFactoryMethod(final Class<?> type) {
        final Collection<Method> factoryMethods = new ArrayList<>(0);
        for (final Method method : type.getMethods()) {
            if (method.getReturnType() == type && (method.getModifiers() & Modifier.STATIC) != 0) {
                factoryMethods.add(method);
            }
        }

        if (factoryMethods.size() > 1) {
            throw MultipleFactoryMethodsException.multipleFactoryMethodsFound(type);
        }

        return factoryMethods.stream()
                .findFirst()
                .orElseThrow(() -> FactoryMethodNotFoundException.factoryMethodNotFound(type));
    }
}
