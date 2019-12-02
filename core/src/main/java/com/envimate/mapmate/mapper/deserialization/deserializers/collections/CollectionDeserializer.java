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

package com.envimate.mapmate.mapper.deserialization.deserializers.collections;

import com.envimate.mapmate.mapper.definitions.CollectionDefinition;
import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.definitions.universal.UniversalCollection;
import com.envimate.mapmate.mapper.deserialization.DeserializerCallback;
import com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer;
import com.envimate.mapmate.mapper.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.mapper.injector.Injector;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ResolvedType;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer.castSafely;

public interface CollectionDeserializer extends TypeDeserializer {
    Object deserialize(List<Object> deserializedElements);

    @Override
    default <T> T deserialize(final Universal input,
                              final Definition definition,
                              final ExceptionTracker exceptionTracker,
                              final Injector injector,
                              final DeserializerCallback callback,
                              final CustomPrimitiveMappings customPrimitiveMappings) {
        final UniversalCollection universalCollection = castSafely(input, UniversalCollection.class, exceptionTracker);
        final List deserializedList = new LinkedList();
        final ResolvedType contentType = ((CollectionDefinition) definition).contentType();
        int index = 0;
        for (final Universal element : universalCollection.content()) {
            final Object deserialized = callback.deserializeRecursive(element, contentType, exceptionTracker.stepIntoArray(index), injector);
            deserializedList.add(deserialized);
            index = index + 1;
        }
        return (T) deserialize(deserializedList);
    }
}
