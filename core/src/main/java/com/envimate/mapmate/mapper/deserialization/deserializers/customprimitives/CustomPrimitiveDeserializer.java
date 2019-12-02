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

package com.envimate.mapmate.mapper.deserialization.deserializers.customprimitives;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.definitions.universal.UniversalObject;
import com.envimate.mapmate.mapper.definitions.universal.UniversalPrimitive;
import com.envimate.mapmate.mapper.deserialization.DeserializerCallback;
import com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer;
import com.envimate.mapmate.mapper.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.mapper.injector.Injector;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ResolvedType;

import java.util.List;

import static com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer.castSafely;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

public interface CustomPrimitiveDeserializer extends TypeDeserializer {

    @Override
    default List<ResolvedType> requiredTypes() {
        return emptyList();
    }

    default Class<?> baseType() {
        return String.class;
    }

    Object deserialize(Object value) throws Exception;

    @Override
    default <T> T deserialize(final Universal input,
                              final Definition definition,
                              final ExceptionTracker exceptionTracker,
                              final Injector injector,
                              final DeserializerCallback callback,
                              final CustomPrimitiveMappings customPrimitiveMappings) {
        final UniversalPrimitive universalPrimitive = castSafely(input, UniversalPrimitive.class, exceptionTracker);
        try {
            final Class<?> baseType = baseType();
            final Object mapped = customPrimitiveMappings.fromUniversal(universalPrimitive, baseType);
            return (T) deserialize(mapped);
        } catch (final Exception e) {
            final String message = format("Exception calling deserialize(input: %s) on definition %s", input.toNativeJava(), definition);
            exceptionTracker.track(e, message);
            return null;
        }
    }
}
