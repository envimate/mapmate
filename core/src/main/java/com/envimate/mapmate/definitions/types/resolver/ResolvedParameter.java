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

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.definitions.types.resolver.TypeResolver.resolveType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResolvedParameter {
    private final FullType fullType;
    private final Parameter parameter;

    public static Optional<List<ResolvedParameter>> resolveParameters(final Executable executable,
                                                                      final FullType fullType) {
        final List<Optional<ResolvedParameter>> optionals = stream(executable.getParameters())
                .map(parameter -> resolveParameter(fullType, parameter))
                .collect(toList());

        if (optionals.stream().anyMatch(Optional::isEmpty)) {
            return empty();
        }

        final List<ResolvedParameter> parameters = optionals.stream()
                .flatMap(Optional::stream)
                .collect(toList());
        return of(parameters);
    }

    public static Optional<ResolvedParameter> resolveParameter(final FullType declaringType,
                                                               final Parameter parameter) {
        validateNotNull(declaringType, "declaringType");
        validateNotNull(parameter, "parameter");

        final Type parameterizedType = parameter.getParameterizedType();
        return resolveType(parameterizedType, declaringType)
                .map(fullType -> new ResolvedParameter(fullType, parameter));
    }

    public FullType type() {
        return this.fullType;
    }

    public Parameter parameter() {
        return this.parameter;
    }
}
