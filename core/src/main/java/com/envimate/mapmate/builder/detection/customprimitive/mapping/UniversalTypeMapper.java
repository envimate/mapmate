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

import com.envimate.mapmate.definitions.universal.Universal;
import com.envimate.mapmate.definitions.universal.UniversalPrimitive;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniversalTypeMapper {
    private final Class<?> normalType;
    private final Class<? extends UniversalPrimitive> universalType;
    private final Function<Object, UniversalPrimitive> toUniversal;
    private final Function<UniversalPrimitive, Object> fromUniversal;

    public static <T, U extends UniversalPrimitive> UniversalTypeMapper universalTypeMapper(final Class<T> normalType,
                                                                                            final Class<U> universalType) {
        validateNotNull(normalType, "normalType");
        validateNotNull(universalType, "universalType");
        return new UniversalTypeMapper(
                normalType, universalType, UniversalPrimitive::universalPrimitive, Universal::toNativeJava);
    }

    @SuppressWarnings("unchecked")
    public static <T, U extends UniversalPrimitive> UniversalTypeMapper universalTypeMapper(final Class<T> normalType,
                                                                                            final Class<U> universalType,
                                                                                            final Function<T, U> toUniversal,
                                                                                            final Function<U, T> fromUniversal) {
        validateNotNull(normalType, "normalType");
        validateNotNull(universalType, "universalType");
        validateNotNull(toUniversal, "toUniversal");
        validateNotNull(fromUniversal, "fromUniversal");
        return new UniversalTypeMapper(
                normalType,
                universalType,
                (Function<Object, UniversalPrimitive>) toUniversal,
                (Function<UniversalPrimitive, Object>) fromUniversal);
    }

    public Class<?> normalType() {
        return this.normalType;
    }

    public UniversalPrimitive toUniversal(final Object object) {
        return this.toUniversal.apply(object);
    }

    public Object fromUniversal(final UniversalPrimitive primitive) {
        return this.fromUniversal.apply(primitive);
    }
}
