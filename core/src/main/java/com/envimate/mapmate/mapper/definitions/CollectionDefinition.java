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

package com.envimate.mapmate.mapper.definitions;

import com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer;
import com.envimate.mapmate.mapper.deserialization.deserializers.collections.CollectionDeserializer;
import com.envimate.mapmate.mapper.serialization.serializers.TypeSerializer;
import com.envimate.mapmate.mapper.serialization.serializers.collections.CollectionSerializer;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionDefinition implements Definition {
    private final ResolvedType type;
    private final ResolvedType contentType;
    private final CollectionSerializer serializer;
    private final CollectionDeserializer deserializer;

    public static CollectionDefinition collectionDefinition(final ResolvedType type,
                                                            final ResolvedType contentType,
                                                            final CollectionSerializer serializer,
                                                            final CollectionDeserializer deserializer) {
        validateNotNull(type, "type");
        validateNotNull(contentType, "contentType");
        validateNotNull(serializer, "serializer");
        validateNotNull(deserializer, "deserializer");
        return new CollectionDefinition(type, contentType, serializer, deserializer);
    }

    public ResolvedType contentType() {
        return this.contentType;
    }

    @Override
    public Optional<TypeSerializer> serializer() {
        return of(this.serializer);
    }

    public Optional<TypeDeserializer> deserializer() {
        return of(this.deserializer);
    }

    @Override
    public ResolvedType type() {
        return this.type;
    }
}
