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

package com.envimate.mapmate.builder.detection.customprimitive.mapping;

import com.envimate.mapmate.definitions.universal.UniversalPrimitive;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveMappings {
    private final Map<Class<?>, UniversalTypeMapper> mappings;

    public static CustomPrimitiveMappings customPrimitiveMappings(final UniversalTypeMapper... mappings) {
        validateNotNull(mappings, "mappings");
        final Map<Class<?>, UniversalTypeMapper> map = Arrays.stream(mappings)
                .collect(toMap(UniversalTypeMapper::normalType, identity()));
        return new CustomPrimitiveMappings(map);
    }

    public boolean isPrimitiveType(final Class<?> type) {
        validateNotNull(type, "type");
        return this.mappings.containsKey(type);
    }

    public Optional<UniversalTypeMapper> byType(final Class<?> type) {
        validateNotNull(type, "type");
        if (!this.mappings.containsKey(type)) {
            return empty();
        }
        return Optional.of(this.mappings.get(type));
    }

    public UniversalPrimitive toUniversal(final Object object) {
        validateNotNull(object, "object");
        if(!this.mappings.containsKey(object.getClass())) {
            throw new UnsupportedOperationException(format("Type '%s' is not registered as a primitive", object.getClass()));
        }
        final UniversalTypeMapper universalTypeMapper = this.mappings.get(object.getClass());
        return universalTypeMapper.toUniversal(object);
    }

    public Object fromUniversal(final UniversalPrimitive universal, final Class<?> type) {
        validateNotNull(universal, "universal");
        validateNotNull(type, "type");
        if(!this.mappings.containsKey(type)) {
            throw new UnsupportedOperationException(format("Type '%s' is not registered as a primitive", type));
        }
        final UniversalTypeMapper universalTypeMapper = this.mappings.get(type);
        return universalTypeMapper.fromUniversal(universal);
    }
}
