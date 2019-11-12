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

package com.envimate.mapmate.builder.definitions;

import com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveDeserializer;
import com.envimate.mapmate.builder.definitions.serializers.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;

import static com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveByMethodDeserializer.createDeserializer;
import static com.envimate.mapmate.builder.definitions.serializers.CustomPrimitiveByMethodSerializer.createSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveDefinition {
    public final Class<?> type;
    public final CustomPrimitiveSerializer<?> serializer;
    public final CustomPrimitiveDeserializer<?> deserializer;

    public static <T> CustomPrimitiveDefinition customPrimitiveDefinition(final Class<T> type,
                                                                          final CustomPrimitiveSerializer<T> serializer,
                                                                          final CustomPrimitiveDeserializer<T> deserializer) {
        return untypedCustomPrimitiveDefinition(type, serializer, deserializer);
    }

    public static CustomPrimitiveDefinition customPrimitiveDefinition(final Class<?> type,
                                                                      final Method serializationMethod,
                                                                      final Method deserializationMethod) {
        final CustomPrimitiveSerializer<?> serializer = createSerializer(type, serializationMethod);
        final CustomPrimitiveDeserializer<?> deserializer = createDeserializer(type, deserializationMethod);
        return untypedCustomPrimitiveDefinition(type, serializer, deserializer);
    }

    public static CustomPrimitiveDefinition untypedCustomPrimitiveDefinition(
            final Class<?> type,
            final CustomPrimitiveSerializer<?> serializer,
            final CustomPrimitiveDeserializer<?> deserializer) {
        validateNotNull(type, "type");
        validateNotNull(serializer, "serializer");
        validateNotNull(deserializer, "deserializer");
        return new CustomPrimitiveDefinition(type, serializer, deserializer);
    }
}
