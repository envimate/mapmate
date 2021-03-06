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

package com.envimate.mapmate.filters;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class FindDtoByFactoryMethodClassFilter implements ClassFilter {
    private final Predicate<Method> additionalFilter;

    static ClassFilter findDtoByFactoryMethodClassFilter() {
        return new FindDtoByFactoryMethodClassFilter(method -> true);
    }

    static ClassFilter findDtoByFactoryMethodNamedClassFilter(final String name) {
        return new FindDtoByFactoryMethodClassFilter(method -> name.equals(method.getName()));
    }

    @Override
    public boolean include(final Class<?> type) {
        final Method[] methods = type.getMethods();
        return stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(type))
                .filter(this.additionalFilter)
                .anyMatch(method ->
                        stream(method.getParameterTypes())
                                .anyMatch(aClass -> !aClass.equals(String.class))
                );
    }
}
