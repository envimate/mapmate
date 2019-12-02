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

package com.envimate.mapmate.scanner.builder.detection.customprimitive.mapping;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;

import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mapping {
    private final Class<?> from;
    private final Class<?> to;
    private final Function<Object, Object> mapping;

    @SuppressWarnings("unchecked")
    public static <A, B> Mapping mapping(final Class<A> from, final Class<B> to, final Function<A, B> mapping) {
        validateNotNull(from, "from");
        validateNotNull(to, "to");
        validateNotNull(mapping, "mapping");
        return new Mapping(from, to, (Function<Object, Object>) mapping);
    }

    public Class<?> from() {
        return this.from;
    }

    public Class<?> to() {
        return this.to;
    }

    @SuppressWarnings("unchecked")
    public <T> T map(final Object from) {
        return (T) this.mapping.apply(from);
    }
}
