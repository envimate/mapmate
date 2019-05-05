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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.envimate.mapmate.filters.FindCustomTypesByFactoryMethodClassFilter.findCustomTypesByFactoryMethodClassFilter;
import static com.envimate.mapmate.filters.FindDtoByFactoryMethodClassFilter.findDtoByFactoryMethodClassFilter;
import static com.envimate.mapmate.filters.PublicStringMethodWithZeroArguments.allClassesWithAPublicStringMethodWithZeroArgumentsNamed;

public final class ClassFilters {

    private ClassFilters() {
    }

    public static ClassFilter includingAll() {
        return type -> true;
    }

    public static ClassFilter allBut(final ClassFilter inverted) {
        return type -> !inverted.include(type);
    }

    public static ClassFilter excluding(final Class<?>... excludes) {
        final Set<Class<?>> excludesSet = Arrays.stream(excludes).collect(Collectors.toSet());
        return type -> !excludesSet.contains(type);
    }

    public static ClassFilter and(final ClassFilter... filters) {
        return type -> Arrays.stream(filters).allMatch(classFilter -> classFilter.include(type));
    }

    public static ClassFilter or(final ClassFilter... filters) {
        return type -> Arrays.stream(filters).anyMatch(classFilter -> classFilter.include(type));
    }

    public static ClassFilter allClassesThatHaveAStaticFactoryMethodWithNonStringArguments() {
        return findDtoByFactoryMethodClassFilter();
    }

    public static ClassFilter allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument() {
        return findCustomTypesByFactoryMethodClassFilter();
    }

    public static ClassFilter allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed(final String methodName) {
        return allClassesWithAPublicStringMethodWithZeroArgumentsNamed(methodName);
    }
}
