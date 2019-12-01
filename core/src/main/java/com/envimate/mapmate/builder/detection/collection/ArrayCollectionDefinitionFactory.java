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

package com.envimate.mapmate.builder.detection.collection;

import com.envimate.mapmate.builder.RequiredCapabilities;
import com.envimate.mapmate.builder.SeedReason;
import com.envimate.mapmate.builder.detection.DefinitionFactory;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.deserialization.deserializers.collections.CollectionDeserializer;
import com.envimate.mapmate.serialization.serializers.collections.CollectionSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.mapmate.definitions.CollectionDefinition.collectionDefinition;
import static com.envimate.mapmate.definitions.types.TypeVariableName.arrayComponentName;
import static com.envimate.mapmate.deserialization.deserializers.collections.ArrayCollectionDeserializer.arrayDeserializer;
import static com.envimate.mapmate.serialization.serializers.collections.ArrayCollectionSerializer.arraySerializer;
import static java.lang.String.format;
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
    public Optional<Definition> analyze(final SeedReason reason,
                                        final FullType type,
                                        final RequiredCapabilities capabilities) {
        if (!type.type().isArray()) {
            return empty();
        }

        if (type.typeParameters().size() != 1) {
            throw new UnsupportedOperationException(format(
                    "This should never happen. An array of type '%s' has more than one type parameter", type.description()));
        }
        final FullType genericType = type.typeParameters().get(arrayComponentName());

        final CollectionSerializer serializer = arraySerializer();
        final CollectionDeserializer deserializer = arrayDeserializer(genericType.type());
        return of(collectionDefinition(reason, type, genericType, serializer, deserializer));
    }
}
