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

package com.envimate.mapmate.definitions;

import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.definitions.universal.Universal;
import com.envimate.mapmate.deserialization.deserializers.customprimitives.CustomPrimitiveDeserializer;
import com.envimate.mapmate.serialization.serializers.customprimitives.CustomPrimitiveSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveDefinition implements Definition {
    private final FullType type;
    private final CustomPrimitiveSerializer serializer;
    private final CustomPrimitiveDeserializer deserializer;

    public static <T, U extends Universal> CustomPrimitiveDefinition customPrimitiveDefinition(final FullType type,
                                                                                               final CustomPrimitiveSerializer serializer,
                                                                                               final CustomPrimitiveDeserializer deserializer) {
        return untypedCustomPrimitiveDefinition(type, serializer, deserializer);
    }

    public static CustomPrimitiveDefinition untypedCustomPrimitiveDefinition(
            final FullType type,
            final CustomPrimitiveSerializer serializer,
            final CustomPrimitiveDeserializer deserializer) {
        validateNotNull(type, "type");
        return new CustomPrimitiveDefinition(type, serializer, deserializer);
    }

    public Optional<CustomPrimitiveDeserializer> deserializer() {
        return ofNullable(this.deserializer);
    }

    public Optional<CustomPrimitiveSerializer> serializer() {
        return ofNullable(this.serializer);
    }

    @Override
    public FullType type() {
        return this.type;
    }
}
