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

package com.envimate.mapmate.mapper.serialization.serializers.collections;

import com.envimate.mapmate.mapper.definitions.CollectionDefinition;
import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.serialization.SerializationCallback;
import com.envimate.mapmate.mapper.serialization.serializers.TypeSerializer;
import com.envimate.mapmate.mapper.serialization.tracker.SerializationTracker;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ResolvedType;

import java.util.List;

import static com.envimate.mapmate.mapper.definitions.universal.UniversalCollection.universalCollection;
import static java.util.stream.Collectors.toList;

public interface CollectionSerializer extends TypeSerializer {
    List<Object> serialize(Object collection);

    @Override
    default Universal serialize(final Definition definition,
                                final Object object,
                                final SerializationCallback callback,
                                final SerializationTracker tracker,
                                final CustomPrimitiveMappings customPrimitiveMappings) {
        final ResolvedType contentType = ((CollectionDefinition) definition).contentType();
        final List<Universal> list = serialize(object)
                .stream()
                .map(element -> callback.serializeDefinition(contentType, element, tracker))
                .collect(toList());
        return universalCollection(list);
    }
}
