/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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
import com.envimate.mapmate.serialization.builder.SerializerBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.envimate.mapmate.serialization.builder.SerializerBuilder.aSerializerBuilder;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@SuppressWarnings("rawtypes")
public final class Serializer {
    private final Marshaller marshaller;
    private final CircularReferenceDetector circularReferenceDetector;
    private final SerializableDefinitions definitions;

    private Serializer(final Marshaller marshaller,
                       final CircularReferenceDetector circularReferenceDetector,
                       final SerializableDefinitions definitions) {
        this.marshaller = marshaller;
        this.circularReferenceDetector = circularReferenceDetector;
        this.definitions = definitions;
    }

    public static Serializer theSerializer(final Marshaller marshaller,
                                           final SerializableDefinitions definitions) {
        final CircularReferenceDetector circularReferenceDetector = new CircularReferenceDetector();
        return new Serializer(marshaller, circularReferenceDetector, definitions);
    }

    public static SerializerBuilder aSerializer() {
        return aSerializerBuilder();
    }

    public String serialize(final Object object) {
        return serialize(object, input -> input);
    }

    @SuppressWarnings("unchecked")
    public String serialize(final Object object,
                            final Function<Map<String, Object>, Map<String, Object>> jsonInjector) {
        validateNotNull(object, "object");
        Object normalized = normalize(object);
        if(normalized instanceof Map) {
            normalized = jsonInjector.apply((Map<String, Object>) normalized);
        }
        return this.marshaller.marshal(normalized);
    }

    private Object normalize(final Object object) {
        this.circularReferenceDetector.detect(object);

        if (Objects.isNull(object)) {
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
        if(definition instanceof SerializableCustomPrimitive) {
            final SerializableCustomPrimitive customPrimitive = (SerializableCustomPrimitive) definition;
            return customPrimitive.serialize(object);
        }
        if(definition instanceof SerializableDataTransferObject) {
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
