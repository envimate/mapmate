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

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.builder.SerializerBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.serialization.builder.SerializerBuilder.aSerializerBuilder;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Objects.isNull;

@SuppressWarnings("rawtypes")
public final class Serializer {
    private final MarshallerRegistry<Marshaller> marshallers;
    private final CircularReferenceDetector circularReferenceDetector;
    private final SerializableDefinitions definitions;

    private Serializer(final MarshallerRegistry<Marshaller> marshallers,
                       final CircularReferenceDetector circularReferenceDetector,
                       final SerializableDefinitions definitions) {
        this.marshallers = marshallers;
        this.circularReferenceDetector = circularReferenceDetector;
        this.definitions = definitions;
    }

    public static Serializer theSerializer(final MarshallerRegistry<Marshaller> marshallers,
                                           final SerializableDefinitions definitions) {
        final CircularReferenceDetector circularReferenceDetector = new CircularReferenceDetector();
        return new Serializer(marshallers, circularReferenceDetector, definitions);
    }

    public static SerializerBuilder aSerializer() {
        return aSerializerBuilder();
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
                            final Function<Map<String, Object>, Map<String, Object>> jsonInjector) {
        validateNotNull(object, "object");
        Object normalized = normalize(object);
        if (normalized instanceof Map) {
            normalized = jsonInjector.apply((Map<String, Object>) normalized);
        }
        return this.marshallers
                .getForType(marshallingType)
                .marshal(normalized);
    }

    public String serializeFromMap(final Map<String, Object> map,
                                   final MarshallingType marshallingType) {
        return this.marshallers.getForType(marshallingType).marshal(map);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> serializeToMap(final Object object) {
        if (isNull(object)) {
            return new HashMap<>();
        }
        final Object normalized = normalize(object);
        if (!(normalized instanceof Map)) {
            throw new UnsupportedOperationException("Only DTOs can be serialized to map");
        }
        return (Map<String, Object>) normalized;
    }

    private Object normalize(final Object object) {
        this.circularReferenceDetector.detect(object);

        if (isNull(object)) {
            return null;
        }

        if (object instanceof Collection<?>) {
            return serializeCollection((Collection<?>) object);
        } else if (object.getClass().isArray()) {
            return serializeArray((Object[]) object);
        } else if (object instanceof Map<?, ?>) {
            return serializeMap(object);
        }

        return serializeDefinition(object);
    }

    private Object serializeDefinition(final Object object) {
        final Definition definition = this.definitions.getDefinitionForObject(object);
        if (definition instanceof SerializableCustomPrimitive) {
            final SerializableCustomPrimitive customPrimitive = (SerializableCustomPrimitive) definition;
            return customPrimitive.serialize(object);
        }
        if (definition instanceof SerializableDataTransferObject) {
            final SerializableDataTransferObject dataTransferObject = (SerializableDataTransferObject) definition;
            return dataTransferObject.serialize(object, this::normalize);
        }
        throw new UnsupportedOperationException("This should never happen.");
    }

    private Object serializeMap(final Object object) {
        final Map<?, ?> castMap = (Map<?, ?>) object;
        final Map<Object, Object> normalizedMap = new HashMap<>(castMap.size());
        ((Map<?, ?>) object).forEach((key, value) -> {
            normalizedMap.put(normalize(key), normalize(value));
        });
        return normalizedMap;
    }

    private Object serializeArray(final Object[] object) {
        return Arrays.stream(object)
                .map(this::normalize).toArray();
    }

    private Object serializeCollection(final Collection<?> object) {
        return object.stream()
                .map(this::normalize)
                .collect(Collectors.toList());
    }

    public SerializableDefinitions getDefinitions() {
        return this.definitions;
    }
}
