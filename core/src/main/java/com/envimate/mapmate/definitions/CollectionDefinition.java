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
import com.envimate.mapmate.deserialization.deserializers.collections.CollectionDeserializer;
import com.envimate.mapmate.serialization.serializers.collections.CollectionSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionDefinition implements Definition {
    private final FullType type;
    private final FullType contentType;
    private final CollectionSerializer serializer;
    private final CollectionDeserializer deserializer;

    public static CollectionDefinition collectionDefinition(final FullType type,
                                                            final FullType contentType,
                                                            final CollectionSerializer serializer,
                                                            final CollectionDeserializer deserializer) {
        validateNotNull(type, "type");
        validateNotNull(contentType, "contentType");
        validateNotNull(serializer, "serializer");
        validateNotNull(deserializer, "deserializer");
        return new CollectionDefinition(type, contentType, serializer, deserializer);
    }

    public FullType contentType() {
        return this.contentType;
    }

    public CollectionSerializer serializer() {
        return this.serializer;
    }

    public CollectionDeserializer deserializer() {
        return this.deserializer;
    }

    @Override
    public FullType type() {
        return this.type;
    }
}
