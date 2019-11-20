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

import com.envimate.mapmate.builder.detection.DefinitionFactory;
import com.envimate.mapmate.builder.detection.serializedobject.deserialization.SerializedObjectDeserializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.fields.FieldDetector;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.hub.FullType;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.ClassFilter.allowAll;
import static com.envimate.mapmate.builder.detection.serializedobject.CodeNeedsToBeCompiledWithParameterNamesException.validateParameterNamesArePresent;
import static com.envimate.mapmate.builder.detection.serializedobject.fields.ModifierFieldDetector.modifierBased;
import static com.envimate.mapmate.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer.serializedObjectSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectDefinitionFactory implements DefinitionFactory {
    private final ClassFilter filter;
    private final FieldDetector fieldDetector;
    private final List<SerializedObjectDeserializationDetector> detectors;

    public static DefinitionFactory serializedObjectFactory(
            final SerializedObjectDeserializationDetector... detectors) {
        return serializedObjectFactory(allowAll(), detectors);
    }

    public static DefinitionFactory serializedObjectFactory(
            final ClassFilter filter,
            final SerializedObjectDeserializationDetector... detectors) {
        return serializedObjectFactory(filter, modifierBased(), detectors);
    }

    public static DefinitionFactory serializedObjectFactory(
            final ClassFilter filter,
            final FieldDetector fieldDetector,
            final SerializedObjectDeserializationDetector... detectors) {
        validateNotNull(filter, "filter");
        validateNotNull(fieldDetector, "fieldDetector");
        validateNotNull(detectors, "detectors");
        return new SerializedObjectDefinitionFactory(filter, fieldDetector, asList(detectors));
    }

    @Override
    public Optional<Definition> analyze(final FullType type) {
        if (!this.filter.filter(type)) {
            return empty();
        }
        validateParameterNamesArePresent(type.type());
        final SerializationFields serializationFields = this.fieldDetector.detect(type);
        return this.detectors.stream()
                .map(detector -> detector.detect(type, serializationFields))
                .flatMap(Optional::stream)
                .findFirst()
                .map(deserializationDTOMethod -> {
                    final SerializedObjectSerializer serializer = serializedObjectSerializer(type, serializationFields);
                    return serializedObjectDefinition(type, serializer, deserializationDTOMethod);
                });
    }
}
