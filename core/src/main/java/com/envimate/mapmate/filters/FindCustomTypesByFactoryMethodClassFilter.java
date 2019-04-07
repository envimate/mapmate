/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

final class FindCustomTypesByFactoryMethodClassFilter implements ClassFilter {

    private FindCustomTypesByFactoryMethodClassFilter() {
    }

    static ClassFilter findCustomTypesByFactoryMethodClassFilter() {
        return new FindCustomTypesByFactoryMethodClassFilter();
    }

    @Override
    public boolean include(final Class<?> type) {
        final Method[] methods = type.getMethods();
        return stream(methods)
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getReturnType().equals(type))
                .anyMatch(method -> method.getParameterTypes()[0].equals(String.class));
    }
}
