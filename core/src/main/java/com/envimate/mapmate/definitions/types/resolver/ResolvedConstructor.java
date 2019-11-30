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

package com.envimate.mapmate.definitions.types.resolver;

import com.envimate.mapmate.definitions.types.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.definitions.types.resolver.ResolvedParameter.resolveParameters;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResolvedConstructor {
    private final List<ResolvedParameter> parameters;
    private final Constructor<?> constructor;

    public static List<ResolvedConstructor> resolvePublicConstructors(final FullType fullType) {
        return stream(fullType.type().getConstructors())
                .filter(constructor -> isPublic(constructor.getModifiers()))
                .map(constructor -> resolveConstructor(constructor, fullType))
                .flatMap(Optional::stream)
                .collect(toList());
    }

    public static Optional<ResolvedConstructor> resolveConstructor(final Constructor<?> constructor,
                                                                   final FullType fullType) {
        return resolveParameters(constructor, fullType)
                .map(parameters -> new ResolvedConstructor(parameters, constructor));
    }

    public List<ResolvedParameter> parameters() {
        return unmodifiableList(this.parameters);
    }

    public Constructor<?> constructor() {
        return this.constructor;
    }
}
