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

package com.envimate.mapmate.shared.types.unresolved.breaking;

import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;

import static com.envimate.mapmate.shared.types.unresolved.UnresolvedType.unresolvedType;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldTypeVariableResolver implements TypeVariableResolver {
    private final Field field;

    public static TypeVariableResolver fieldTypeVariableResolver(final Field field) {
        validateNotNull(field, "field");
        return new FieldTypeVariableResolver(field);
    }

    @Override
    public ResolvedType resolve(final Object object) {
        try {
            final Object value = this.field.get(object);
            return unresolvedType(value.getClass()).resolveFromObject(value);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
