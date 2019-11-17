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

package com.envimate.mapmate.builder.recipes.manualregistry;

import com.envimate.mapmate.MapMateBuilder;
import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.builder.detection.serializedobject.fields.FieldDetector;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.envimate.mapmate.builder.detection.serializedobject.fields.ModifierFieldDetector.modifierBased;
import static com.envimate.mapmate.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static com.envimate.mapmate.deserialization.deserializers.serializedobjects.MethodSerializedObjectDeserializer.methodNameDeserializer;
import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer.serializedObjectSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManualRegistry implements Recipe {
    private static final FieldDetector FIELD_DETECTOR = modifierBased();

    private final List<CustomPrimitiveDefinition> customPrimitiveDefinitions = new LinkedList<>();
    private final List<SerializedObjectDefinition> serializedObjectDefinitions = new LinkedList<>();
    private final List<Class<?>> manuallyAddedCustomPrimitiveTypes = new LinkedList<>();
    private final List<Class<?>> manuallyAddedSerializedObjectTypes = new LinkedList<>();

    public static ManualRegistry manuallyRegisteredTypes() {
        return new ManualRegistry();
    }

    public <T> ManualRegistry withCustomPrimitive(final Class<T> type,
                                                  final Function<T, String> serializationMethod,
                                                  final Function<String, T> deserializationMethod) {
        return this.withCustomPrimitive(customPrimitiveDefinition(
                type,
                serializationMethod::apply,
                deserializationMethod::apply
        ));
    }

    public ManualRegistry withCustomPrimitive(final CustomPrimitiveDefinition customPrimitive) {
        if (this.customPrimitiveDefinitions.contains(customPrimitive)) {
            throw new UnsupportedOperationException(String.format(
                    "The customPrimitive %s has already been added for type %s",
                    customPrimitive,
                    customPrimitive.type()));
        }
        this.customPrimitiveDefinitions.add(customPrimitive);

        return this;
    }

    public ManualRegistry withCustomPrimitives(final Class<?>... customPrimitiveTypes) {
        stream(customPrimitiveTypes).forEach(type -> validateNotNull(type, "type"));
        this.manuallyAddedCustomPrimitiveTypes.addAll(asList(customPrimitiveTypes));
        return this;
    }

    public ManualRegistry withSerializedObject(final SerializedObjectDefinition serializedObject) {
        if (this.serializedObjectDefinitions.contains(serializedObject)) {
            throw new UnsupportedOperationException(String.format(
                    "The serializedObject %s has already been added for type %s",
                    serializedObject,
                    serializedObject.type()));
        }
        this.serializedObjectDefinitions.add(serializedObject);
        return this;
    }

    public ManualRegistry withSerializedObject(final Class<?> type,
                                               final Field[] serializedFields,
                                               final String deserializationMethodName) {
        final SerializedObjectDeserializer deserializer = methodNameDeserializer(type, deserializationMethodName, serializedFields);
        final SerializationFields serializationFields = FIELD_DETECTOR.detect(type);
        final SerializedObjectSerializer serializer = serializedObjectSerializer(type, serializationFields);
        final SerializedObjectDefinition serializedObject = serializedObjectDefinition(type, serializer, deserializer);
        return this.withSerializedObject(serializedObject);
    }

    public ManualRegistry withSerializedObjects(final Class<?>... serializedObjectTypes) {
        stream(serializedObjectTypes).forEach(type -> validateNotNull(type, "type"));
        this.manuallyAddedSerializedObjectTypes.addAll(asList(serializedObjectTypes));
        return this;
    }

    @Override
    public List<CustomPrimitiveDefinition> customPrimitiveDefinitions() {
        return unmodifiableList(this.customPrimitiveDefinitions);
    }

    @Override
    public List<SerializedObjectDefinition> serializedObjectDefinitions() {
        return unmodifiableList(this.serializedObjectDefinitions);
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder) {
        final Detector detector = mapMateBuilder.detector;

        final List<Definition> definitions = this.manuallyAddedCustomPrimitiveTypes.stream()
                .map(detector::detect)
                .flatMap(Optional::stream)
                .collect(toList());

        this.manuallyAddedSerializedObjectTypes.stream()
                .map(detector::detect)
                .flatMap(Optional::stream)
                .forEach(definitions::add);

        definitions.stream()
                .filter(Definition::isCustomPrimitive)
                .map(definition -> (CustomPrimitiveDefinition) definition)
                .forEach(this.customPrimitiveDefinitions::add);

        definitions.stream()
                .filter(Definition::isSerializedObject)
                .map(definition -> (SerializedObjectDefinition) definition)
                .forEach(this.serializedObjectDefinitions::add);
    }
}
