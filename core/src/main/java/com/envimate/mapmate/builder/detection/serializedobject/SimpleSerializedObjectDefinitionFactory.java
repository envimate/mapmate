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

package com.envimate.mapmate.builder.detection.serializedobject;

import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.FieldDetector.modifierBased;
import static com.envimate.mapmate.builder.detection.serializedobject.ClassFilter.allowAll;
import static com.envimate.mapmate.builder.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleSerializedObjectDefinitionFactory implements SerializedObjectDefinitionFactory {
    private final ClassFilter filter;
    private final FieldDetector fieldDetector;
    private final List<SerializedObjectDeserializationDetector> detectors;

    public static SerializedObjectDefinitionFactory serializedObjectFactory(
            final SerializedObjectDeserializationDetector... detectors) {
        return serializedObjectFactory(allowAll(), detectors);
    }

    public static SerializedObjectDefinitionFactory serializedObjectFactory(
            final ClassFilter filter,
            final SerializedObjectDeserializationDetector... detectors) {
        return serializedObjectFactory(filter, modifierBased(), detectors);
    }

    public static SerializedObjectDefinitionFactory serializedObjectFactory(
            final ClassFilter filter,
            final FieldDetector fieldDetector,
            final SerializedObjectDeserializationDetector... detectors) {
        validateNotNull(filter, "filter");
        validateNotNull(fieldDetector, "fieldDetector");
        validateNotNull(detectors, "detectors");
        return new SimpleSerializedObjectDefinitionFactory(filter, fieldDetector, asList(detectors));
    }

    @Override
    public Optional<SerializedObjectDefinition> analyze(final Class<?> type) {
        if (!this.filter.filter(type)) {
            return empty();
        }
        final Field[] serializedFields = stream(type.getFields())
                .filter(this.fieldDetector::useForSerialization)
                .toArray(Field[]::new);
        return this.detectors.stream()
                .map(detector -> detector.detect(type, serializedFields))
                .flatMap(Optional::stream)
                .findFirst()
                .map(deserializationDTOMethod -> serializedObjectDefinition(type, serializedFields, deserializationDTOMethod));
    }
}
