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

package com.envimate.mapmate.serialization;

import com.envimate.mapmate.builder.detection.customprimitive.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.definitions.*;
import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.definitions.universal.UniversalCollection;
import com.envimate.mapmate.definitions.universal.UniversalObject;
import com.envimate.mapmate.definitions.universal.Universal;
import com.envimate.mapmate.definitions.universal.UniversalPrimitive;
import com.envimate.mapmate.marshalling.Marshaller;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.mapmate.serialization.tracker.SerializationTracker;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.envimate.mapmate.definitions.types.FullType.typeOfObject;
import static com.envimate.mapmate.definitions.universal.UniversalCollection.universalCollection;
import static com.envimate.mapmate.definitions.universal.UniversalNull.universalNull;
import static com.envimate.mapmate.definitions.universal.UniversalObject.universalObject;
import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.serialization.tracker.SerializationTracker.serializationTracker;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Serializer {
    private final MarshallerRegistry<Marshaller> marshallers;
    private final Definitions definitions;
    private final CustomPrimitiveMappings customPrimitiveMappings;

    public static Serializer theSerializer(final MarshallerRegistry<Marshaller> marshallers,
                                           final Definitions definitions,
                                           final CustomPrimitiveMappings customPrimitiveMappings) {
        return new Serializer(marshallers, definitions, customPrimitiveMappings);
    }

    public Set<MarshallingType> supportedMarshallingTypes() {
        return this.marshallers.supportedTypes();
    }

    public String serializeToJson(final Object object) {
        return serialize(object, json());
    }

    public String serialize(final Object object,
                            final MarshallingType marshallingType) {
        return serialize(object, marshallingType, input -> input);
    }

    @SuppressWarnings("unchecked")
    public String serialize(final Object object,
                            final MarshallingType marshallingType,
                            final Function<Map<String, Object>, Map<String, Object>> serializedPropertyInjector) {
        validateNotNull(object, "object");
        Object normalized = normalize(object);
        if (normalized instanceof Map) {
            normalized = serializedPropertyInjector.apply((Map<String, Object>) normalized);
        }
        final Marshaller marshaller = this.marshallers.getForType(marshallingType);
        try {
            return marshaller.marshal(normalized);
        } catch (final Exception e) {
            throw new UnsupportedOperationException(format("Could not marshal normalization %s", normalized), e);
        }
    }

    public String serializeFromMap(final Map<String, Object> map,
                                   final MarshallingType marshallingType) {
        final Marshaller marshaller = this.marshallers.getForType(marshallingType);
        try {
            return marshaller.marshal(map);
        } catch (final Exception e) {
            throw new UnsupportedOperationException(format("Could not marshal from map %s", map), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> serializeToMap(final Object object) {
        if (isNull(object)) {
            return new HashMap<>(0);
        }
        final Object normalized = normalize(object);
        if (!(normalized instanceof Map)) {
            throw new UnsupportedOperationException("Only serialized objects can be serialized to map");
        }
        return (Map<String, Object>) normalized;
    }

    private Object normalize(final Object object) {
        if (isNull(object)) {
            return null;
        }

        final FullType type = typeOfObject(object);
        return serializeDefinition(type, object, serializationTracker()).toNativeJava();
    }

    private Universal serializeDefinition(final FullType type,
                                          final Object object,
                                          final SerializationTracker tracker) {
        if (isNull(object)) {
            return universalNull();
        }
        tracker.trackToProhibitCyclicReferences(object);
        final Definition definition = this.definitions.getDefinitionForType(type);
        if (definition instanceof CustomPrimitiveDefinition) {
            return serializeCustomPrimitive((CustomPrimitiveDefinition) definition, object);
        }
        if (definition instanceof SerializedObjectDefinition) {
            return serializeSerializedObject((SerializedObjectDefinition) definition, object, tracker);
        }
        if (definition instanceof CollectionDefinition) {
            return serializeCollection((CollectionDefinition) definition, object, tracker);
        }
        throw new UnsupportedOperationException("This should never happen.");
    }

    private UniversalPrimitive serializeCustomPrimitive(final CustomPrimitiveDefinition definition,
                                                        final Object object) {
        final Object serialized = definition.serializer().serialize(object);
        return this.customPrimitiveMappings.toUniversal(serialized);
    }

    private UniversalObject serializeSerializedObject(final SerializedObjectDefinition definition,
                                                      final Object object,
                                                      final SerializationTracker tracker) {
        final SerializationFields fields = definition.serializer().fields();
        final Map<String, Universal> map = new HashMap<>(10);
        fields.fields().forEach(serializationField -> {
            final FullType type = serializationField.type();
            final Object value = ofNullable(object).map(serializationField::query).orElse(null);
            final Universal serializedValue = serializeDefinition(type, value, tracker);
            final String name = serializationField.name();
            map.put(name, serializedValue);
        });
        return universalObject(map);
    }

    private UniversalCollection serializeCollection(final CollectionDefinition definition,
                                                    final Object object,
                                                    final SerializationTracker tracker) {
        final FullType contentType = definition.contentType();
        final List<Universal> list = definition.serializer().serialize(object)
                .stream()
                .map(element -> serializeDefinition(contentType, element, tracker))
                .collect(toList());
        return universalCollection(list);
    }

    public Definitions getDefinitions() {
        return this.definitions;
    }
}
