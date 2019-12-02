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

package com.envimate.mapmate.mapper.serialization;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.Definitions;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.marshalling.Marshaller;
import com.envimate.mapmate.mapper.marshalling.MarshallerRegistry;
import com.envimate.mapmate.mapper.marshalling.MarshallingType;
import com.envimate.mapmate.mapper.serialization.tracker.SerializationTracker;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ClassType;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.envimate.mapmate.mapper.definitions.universal.UniversalNull.universalNull;
import static com.envimate.mapmate.mapper.marshalling.MarshallingType.json;
import static com.envimate.mapmate.mapper.serialization.tracker.SerializationTracker.serializationTracker;
import static com.envimate.mapmate.shared.types.ClassType.typeOfObject;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Serializer implements SerializationCallback {
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

        final ClassType type = typeOfObject(object);
        return serializeDefinition(type, object, serializationTracker()).toNativeJava();
    }

    @Override
    public Universal serializeDefinition(final ResolvedType type,
                                         final Object object,
                                         final SerializationTracker tracker) {
        if (isNull(object)) {
            return universalNull();
        }
        tracker.trackToProhibitCyclicReferences(object);
        final Definition definition = this.definitions.getDefinitionForType(type);
        return definition.serializer()
                .orElseThrow(() -> new UnsupportedOperationException(
                        format("No serializer configured for type '%s'", definition.type().description())))
                .serialize(definition, object, this, tracker, this.customPrimitiveMappings);
    }

    public Definitions getDefinitions() {
        return this.definitions;
    }
}
