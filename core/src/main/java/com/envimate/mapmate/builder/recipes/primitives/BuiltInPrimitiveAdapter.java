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

import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.envimate.mapmate.serialization.methods.SerializationMethodNotCompatibleException.serializationMethodNotCompatibleException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BuiltInPrimitiveAdapter implements SerializationCPMethod, DeserializationCPMethod {
    private final Class<?> type;
    private final Function<String, ?> deserializer;

    public static void forEach(final BiConsumer<Class<?>, BuiltInPrimitiveAdapter> primitiveAdapterConsumer) {
        primitiveAdapterConsumer.accept(int.class, integerPrimitiveAdapter());
        primitiveAdapterConsumer.accept(Integer.class, integerObjectAdapter());
        primitiveAdapterConsumer.accept(long.class, longPrimitiveAdapter());
        primitiveAdapterConsumer.accept(Long.class, longObjectAdapter());
        primitiveAdapterConsumer.accept(short.class, shortPrimitiveAdapter());
        primitiveAdapterConsumer.accept(Short.class, shortObjectAdapter());
        primitiveAdapterConsumer.accept(double.class, doublePrimitiveAdapter());
        primitiveAdapterConsumer.accept(Double.class, doubleObjectAdapter());
        primitiveAdapterConsumer.accept(float.class, floatPrimitiveAdapter());
        primitiveAdapterConsumer.accept(Float.class, floatObjectAdapter());
        primitiveAdapterConsumer.accept(boolean.class, booleanPrimitiveAdapter());
        primitiveAdapterConsumer.accept(Boolean.class, booleanObjectAdapter());
        primitiveAdapterConsumer.accept(String.class, stringObjectAdapter());
    }

    public static BuiltInPrimitiveAdapter integerPrimitiveAdapter() {
        return new BuiltInPrimitiveAdapter(int.class, Integer::parseInt);
    }

    public static BuiltInPrimitiveAdapter integerObjectAdapter() {
        return new BuiltInPrimitiveAdapter(Integer.class, Integer::valueOf);
    }

    public static BuiltInPrimitiveAdapter longPrimitiveAdapter() {
        return new BuiltInPrimitiveAdapter(long.class, Long::parseLong);
    }

    public static BuiltInPrimitiveAdapter longObjectAdapter() {
        return new BuiltInPrimitiveAdapter(Long.class, Long::valueOf);
    }

    public static BuiltInPrimitiveAdapter shortPrimitiveAdapter() {
        return new BuiltInPrimitiveAdapter(short.class, Short::parseShort);
    }

    public static BuiltInPrimitiveAdapter shortObjectAdapter() {
        return new BuiltInPrimitiveAdapter(Short.class, Short::valueOf);
    }

    public static BuiltInPrimitiveAdapter doublePrimitiveAdapter() {
        return new BuiltInPrimitiveAdapter(double.class, Double::parseDouble);
    }

    public static BuiltInPrimitiveAdapter doubleObjectAdapter() {
        return new BuiltInPrimitiveAdapter(Double.class, Double::valueOf);
    }

    public static BuiltInPrimitiveAdapter floatPrimitiveAdapter() {
        return new BuiltInPrimitiveAdapter(float.class, Float::parseFloat);
    }

    public static BuiltInPrimitiveAdapter floatObjectAdapter() {
        return new BuiltInPrimitiveAdapter(Float.class, Float::valueOf);
    }

    public static BuiltInPrimitiveAdapter booleanPrimitiveAdapter() {
        return new BuiltInPrimitiveAdapter(boolean.class, Boolean::parseBoolean);
    }

    public static BuiltInPrimitiveAdapter booleanObjectAdapter() {
        return new BuiltInPrimitiveAdapter(Boolean.class, Boolean::valueOf);
    }

    public static BuiltInPrimitiveAdapter stringObjectAdapter() {
        return new BuiltInPrimitiveAdapter(String.class, Function.identity());
    }

    @Override
    public void verifyCompatibility(final Class<?> targetType) {
        if (!this.type.equals(targetType)) {
            throw serializationMethodNotCompatibleException(String.format(
                    "class '%s' is not supported by %s for type %s",
                    targetType.getCanonicalName(),
                    this.getClass().getCanonicalName(),
                    this.type)
            );
        }
    }

    @Override
    public Object deserialize(final String input, final Class<?> targetType) {
        return this.deserializer.apply(input);
    }

    @Override
    public String serialize(final Object object) {
        return String.valueOf(object);
    }
}
