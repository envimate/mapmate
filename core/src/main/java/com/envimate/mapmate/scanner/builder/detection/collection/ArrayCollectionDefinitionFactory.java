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

package com.envimate.mapmate.scanner.builder.detection.collection;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.deserialization.deserializers.collections.CollectionDeserializer;
import com.envimate.mapmate.mapper.serialization.serializers.collections.CollectionSerializer;
import com.envimate.mapmate.scanner.builder.DefinitionSeed;
import com.envimate.mapmate.scanner.builder.RequiredCapabilities;
import com.envimate.mapmate.scanner.builder.detection.DefinitionFactory;
import com.envimate.mapmate.shared.types.ArrayType;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.mapmate.mapper.definitions.CollectionDefinition.collectionDefinition;
import static com.envimate.mapmate.mapper.deserialization.deserializers.collections.ArrayCollectionDeserializer.arrayDeserializer;
import static com.envimate.mapmate.mapper.serialization.serializers.collections.ArrayCollectionSerializer.arraySerializer;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayCollectionDefinitionFactory implements DefinitionFactory {

    public static DefinitionFactory arrayFactory() {
        return new ArrayCollectionDefinitionFactory();
    }

    @Override
    public Optional<Definition> analyze(final ResolvedType type,
                                        final RequiredCapabilities capabilities) {
        if (!(type instanceof ArrayType)) {
            return empty();
        }

        final ResolvedType genericType = ((ArrayType) type).componentType();

        final CollectionSerializer serializer = arraySerializer();
        final CollectionDeserializer deserializer = arrayDeserializer(genericType.assignableType());
        return of(collectionDefinition(type, genericType, serializer, deserializer));
    }
}
