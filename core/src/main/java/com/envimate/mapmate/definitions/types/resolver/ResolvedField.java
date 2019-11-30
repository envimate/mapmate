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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.definitions.types.resolver.TypeResolver.resolveType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResolvedField {
    private final String name;
    private final FullType type;
    private final Field field;

    public static List<ResolvedField> resolvedPublicFields(final FullType fullType) {
        final Class<?> type = fullType.type();
        return stream(type.getFields())
                .filter(field -> isPublic(field.getModifiers()))
                .filter(field -> !isStatic(field.getModifiers()))
                .filter(field -> !isTransient(field.getModifiers()))
                .map(field -> resolveType(field.getGenericType(), fullType)
                        .map(resolved -> resolvedField(field.getName(), resolved, field)))
                .flatMap(Optional::stream)
                .collect(toList());
    }

    public static ResolvedField resolvedField(final String name, final FullType type, final Field field) {
        validateNotNull(name, "name");
        validateNotNull(type, "type");
        validateNotNull(field, "field");
        return new ResolvedField(name, type, field);
    }

    public String name() {
        return this.name;
    }

    public FullType type() {
        return this.type;
    }

    public Field field() {
        return this.field;
    }
}
