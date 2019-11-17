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

package com.envimate.mapmate.builder.recipes.primitives;

import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.serialization.serializers.customprimitives.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.function.Function;

import static com.envimate.mapmate.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BuiltInPrimitiveSerializedAsStringSupport implements Recipe {
    public static BuiltInPrimitiveSerializedAsStringSupport builtInPrimitiveSerializedAsStringSupport() {
        return new BuiltInPrimitiveSerializedAsStringSupport();
    }

    @Override
    public List<CustomPrimitiveDefinition> customPrimitiveDefinitions() {
        return List.of(
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
                toCustomPrimitiveDefinition(String.class, Function.identity())
        );
    }

    private static <T> CustomPrimitiveDefinition toCustomPrimitiveDefinition(final Class<T> type,
                                                                             final Function<String, T> deserializer) {
        final CustomPrimitiveSerializer<T> customPrimitiveSerializer = obj -> {
            if (obj != null) {
                return String.valueOf(obj);
            } else {
                return null;
            }
        };
        return customPrimitiveDefinition(type, customPrimitiveSerializer, deserializer::apply);
    }
}
