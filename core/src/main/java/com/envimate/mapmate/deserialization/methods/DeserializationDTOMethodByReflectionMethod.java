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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeserializationDTOMethodByReflectionMethod implements DeserializationDTOMethod {
    private final Map<String, Class<?>> elements;
    private final Method factoryMethod;
    private final Parameter[] parameters;

    public static DeserializationDTOMethodByReflectionMethod usingMethod(final Method factoryMethod) {
        if(Objects.isNull(factoryMethod)) {
            throw new IllegalArgumentException("factoryMethod parameter is required");
        }
        if(!Modifier.isStatic(factoryMethod.getModifiers())) {
            throw new IllegalArgumentException(String.format(
                    "factoryMethod %s must be a static method",
                    factoryMethod
            ));
        }

        final Parameter[] parameters = factoryMethod.getParameters();
        final Map<String, Class<?>> elements = Arrays.stream(parameters).collect(Collectors
                .toMap(Parameter::getName, Parameter::getType)
        );
        return new DeserializationDTOMethodByReflectionMethod(elements, factoryMethod, parameters);
    }

    @Override
    public Object deserialize(final Class<?> targetType,
                              final Map<String, Object> elements) throws Exception {
        final Object[] arguments = Arrays.stream(this.parameters)
                .map(parameter -> elements.get(parameter.getName()))
                .toArray();
        return this.factoryMethod.invoke(null, arguments);
    }

    @Override
    public Map<String, Class<?>> elements(final Class<?> targetType) {
        return Collections.unmodifiableMap(this.elements);
    }
}
