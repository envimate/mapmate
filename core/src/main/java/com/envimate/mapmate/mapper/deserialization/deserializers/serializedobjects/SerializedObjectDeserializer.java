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

package com.envimate.mapmate.mapper.deserialization.deserializers.serializedobjects;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.definitions.universal.UniversalObject;
import com.envimate.mapmate.mapper.deserialization.DeserializationFields;
import com.envimate.mapmate.mapper.deserialization.DeserializerCallback;
import com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer;
import com.envimate.mapmate.mapper.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.mapper.injector.Injector;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ResolvedType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.mapper.definitions.universal.UniversalNull.universalNull;
import static com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer.castSafely;
import static java.lang.String.format;

public interface SerializedObjectDeserializer extends TypeDeserializer {

    @Override
    default List<ResolvedType> requiredTypes() {
        return fields().referencedTypes();
    }

    Object deserialize(Map<String, Object> elements) throws Exception;

    DeserializationFields fields();

    @Override
    default <T> T deserialize(final Universal input,
                              final Definition definition,
                              final ExceptionTracker exceptionTracker,
                              final Injector injector,
                              final DeserializerCallback callback,
                              final CustomPrimitiveMappings customPrimitiveMappings) {
        final UniversalObject universalObject = castSafely(input, UniversalObject.class, exceptionTracker);

        final SerializedObjectDeserializer deserializer = (SerializedObjectDeserializer) definition.deserializer()
                .orElseThrow(() -> new UnsupportedOperationException(format("No deserializer configured for '%s'", definition.type().description())));
        final DeserializationFields deserializationFields = deserializer.fields();
        final Map<String, Object> elements = new HashMap<>(0);
        for (final Map.Entry<String, ResolvedType> entry : deserializationFields.fields().entrySet()) {
            final String elementName = entry.getKey();
            final ResolvedType elementType = entry.getValue();

            final Universal elementInput = universalObject.getField(elementName).orElse(universalNull());
            final Object elementObject = callback.deserializeRecursive(
                    elementInput,
                    elementType,
                    exceptionTracker.stepInto(elementName),
                    injector);
            elements.put(elementName, elementObject);
        }

        if (exceptionTracker.validationResult().hasValidationErrors()) {
            return null;
        } else {
            try {
                return (T) deserializer.deserialize(elements);
            } catch (final Exception e) {
                final String message = format("Exception calling deserialize(type: %s, elements: %s) on deserializationMethod %s",
                        definition.type().description(), elements, deserializer);
                exceptionTracker.track(e, message);
                return null;
            }
        }
    }
}
