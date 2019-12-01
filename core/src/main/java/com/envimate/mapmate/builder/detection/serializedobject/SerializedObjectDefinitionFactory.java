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
import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationField;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.envimate.mapmate.builder.detection.serializedobject.ClassFilter.allowAll;
import static com.envimate.mapmate.builder.detection.serializedobject.CodeNeedsToBeCompiledWithParameterNamesException.validateParameterNamesArePresent;
import static com.envimate.mapmate.builder.detection.serializedobject.fields.ModifierFieldDetector.modifierBased;
import static com.envimate.mapmate.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields.serializationFields;
import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer.serializedObjectSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectDefinitionFactory implements DefinitionFactory {
    private final ClassFilter filter;
    private final List<FieldDetector> fieldDetectors;
    private final List<SerializedObjectDeserializationDetector> detectors;

    public static DefinitionFactory serializedObjectFactory(
            final List<SerializedObjectDeserializationDetector> detectors) {
        return serializedObjectFactory(allowAll(), detectors);
    }

    public static DefinitionFactory serializedObjectFactory(
            final ClassFilter filter,
            final List<SerializedObjectDeserializationDetector> detectors) {
        return serializedObjectFactory(filter, singletonList(modifierBased()), detectors);
    }

    public static DefinitionFactory serializedObjectFactory(
            final ClassFilter filter,
            final List<FieldDetector> fieldDetectors,
            final List<SerializedObjectDeserializationDetector> detectors) {
        validateNotNull(filter, "filter");
        validateNotNull(fieldDetectors, "fieldDetectors");
        validateNotNull(detectors, "detectors");
        return new SerializedObjectDefinitionFactory(filter, fieldDetectors, detectors);
    }

    @Override
    public Optional<Definition> analyze(final FullType type) {
        if (!this.filter.filter(type)) {
            return empty();
        }
        validateParameterNamesArePresent(type.type());
        final List<SerializationField> serializationFieldsList = this.fieldDetectors.stream()
                .map(fieldDetector -> fieldDetector.detect(type))
                .flatMap(Collection::stream)
                .filter(distinctByKey(SerializationField::name))
                .collect(toList());
        final SerializationFields serializationFields = serializationFields(serializationFieldsList);
        final Optional<SerializedObjectSerializer> serializer = serializedObjectSerializer(type, serializationFields);

        final Optional<SerializedObjectDeserializer> deserializer = this.detectors.stream()
                .map(detector -> detector.detect(type, serializationFields))
                .flatMap(Optional::stream)
                .findFirst();

        if (serializer.isPresent() || deserializer.isPresent()) {
            return of(serializedObjectDefinition(type, serializer.orElse(null), deserializer.orElse(null)));
        }
        return empty();
    }

    public static <T> Predicate<T> distinctByKey(final Function<T, String> key) {
        final Set<String> alreadySeenKeys = ConcurrentHashMap.newKeySet();
        return element -> alreadySeenKeys.add(key.apply(element));
    }
}
