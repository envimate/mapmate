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

package com.envimate.mapmate.builder.detection.customprimitive;

import com.envimate.mapmate.builder.RequiredCapabilities;
import com.envimate.mapmate.builder.SeedReason;
import com.envimate.mapmate.builder.detection.DefinitionFactory;
import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.serialization.serializers.customprimitives.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.envimate.mapmate.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.definitions.types.FullType.fullType;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.function.Function.identity;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BuiltInPrimitivesFactory implements DefinitionFactory {
    private static final Map<FullType, Definition> PRIMITIVE_DEFINITIONS = Stream.of(
            toCustomPrimitiveDefinition(int.class, Integer::parseInt),
            toCustomPrimitiveDefinition(Integer.class, Integer::valueOf),
            toCustomPrimitiveDefinition(long.class, Long::parseLong),
            toCustomPrimitiveDefinition(Long.class, Long::valueOf),
            toCustomPrimitiveDefinition(short.class, Short::parseShort),
            toCustomPrimitiveDefinition(Short.class, Short::valueOf),
            toCustomPrimitiveDefinition(double.class, Double::parseDouble),
            toCustomPrimitiveDefinition(Double.class, Double::valueOf),
            toCustomPrimitiveDefinition(float.class, Float::parseFloat),
            toCustomPrimitiveDefinition(Float.class, Float::valueOf),
            toCustomPrimitiveDefinition(boolean.class, Boolean::parseBoolean),
            toCustomPrimitiveDefinition(Boolean.class, Boolean::valueOf),
            toCustomPrimitiveDefinition(String.class, identity()))
            .collect(Collectors.toMap(CustomPrimitiveDefinition::type, identity()));

    public static DefinitionFactory builtInPrimitivesFactory() {
        return new BuiltInPrimitivesFactory();
    }

    @Override
    public Optional<Definition> analyze(final SeedReason reason,
                                        final FullType type,
                                        final RequiredCapabilities capabilities) {
        if (PRIMITIVE_DEFINITIONS.containsKey(type)) {
            return of(PRIMITIVE_DEFINITIONS.get(type).apply(reason));
        }
        return empty();
    }

    private static <T> CustomPrimitiveDefinition toCustomPrimitiveDefinition(final Class<T> type,
                                                                             final Function<String, T> deserializer) {
        final CustomPrimitiveSerializer customPrimitiveSerializer = obj -> {
            if (obj != null) {
                return String.valueOf(obj);
            } else {
                return null;
            }
        };
        return customPrimitiveDefinition(
                fullType(type),
                customPrimitiveSerializer,
                value -> deserializer.apply((String) value));
    }
}
