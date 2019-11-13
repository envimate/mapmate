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

package com.envimate.mapmate.builder.detection.customprimitive;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import com.envimate.mapmate.builder.definitions.deserializers.CustomPrimitiveDeserializer;
import com.envimate.mapmate.builder.definitions.serializers.CustomPrimitiveSerializer;
import com.envimate.mapmate.reflections.CachedReflectionType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.untypedCustomPrimitiveDefinition;
import static com.envimate.mapmate.reflections.CachedReflectionType.cachedReflectionType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleCustomPrimitiveDefinitionFactory implements CustomPrimitiveDefinitionFactory {
    private final CustomPrimitiveSerializationDetector serializationDetector;
    private final List<CustomPrimitiveDeserializationDetector> deserializationDetectors;

    public static CustomPrimitiveDefinitionFactory definitionFactory(
            final CustomPrimitiveSerializationDetector serializationDetector,
            final CustomPrimitiveDeserializationDetector... deserializationDetectors) {
        validateNotNull(serializationDetector, "serializationDetector");
        validateNotNull(deserializationDetectors, "deserializationDetectors");
        return new SimpleCustomPrimitiveDefinitionFactory(serializationDetector, asList(deserializationDetectors));
    }

    @Override
    public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
        final CachedReflectionType cachedReflectionType = cachedReflectionType(type);
        final Optional<CustomPrimitiveSerializer<?>> serializer = this.serializationDetector.detect(cachedReflectionType);
        final Optional<CustomPrimitiveDeserializer<?>> deserializer = this.deserializationDetectors.stream()
                .map(detector -> detector.detect(cachedReflectionType))
                .flatMap(Optional::stream)
                .findFirst();
        if (serializer.isPresent() && deserializer.isPresent()) {
            return of(untypedCustomPrimitiveDefinition(type, serializer.get(), deserializer.get()));
        }
        return empty();
    }
}
