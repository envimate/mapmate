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
import java.util.function.Function;

import static com.envimate.mapmate.builder.detection.serializedobject.fields.ModifierFieldDetector.modifierBased;
import static com.envimate.mapmate.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static com.envimate.mapmate.definitions.types.FullType.fullType;
import static com.envimate.mapmate.deserialization.deserializers.serializedobjects.MethodSerializedObjectDeserializer.methodNameDeserializer;
import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer.serializedObjectSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManualRegistry implements Recipe {
    private static final FieldDetector FIELD_DETECTOR = modifierBased();

    private final List<Definition> definitions = new LinkedList<>();
    private final List<Class<?>> manuallyAddedCustomPrimitiveTypes = new LinkedList<>();
    private final List<Class<?>> manuallyAddedSerializedObjectTypes = new LinkedList<>();

    public static ManualRegistry manuallyRegisteredTypes() {
        return new ManualRegistry();
    }

    @SuppressWarnings("unchecked")
    public <T> ManualRegistry withCustomPrimitive(final Class<T> type,
                                                  final Function<T, String> serializationMethod,
                                                  final Function<String, T> deserializationMethod) {
        return this.withCustomPrimitive(customPrimitiveDefinition(
                fullType(type),
                object -> serializationMethod.apply((T) object),
                deserializationMethod::apply
        ));
    }

    public ManualRegistry withCustomPrimitive(final CustomPrimitiveDefinition customPrimitive) {
        if (this.definitions.contains(customPrimitive)) {
            throw new UnsupportedOperationException(format(
                    "The customPrimitive %s has already been added for type %s", customPrimitive, customPrimitive.type().description()));
        }
        this.definitions.add(customPrimitive);

        return this;
    }

    public ManualRegistry withCustomPrimitives(final Class<?>... customPrimitiveTypes) {
        stream(customPrimitiveTypes).forEach(type -> validateNotNull(type, "type"));
        this.manuallyAddedCustomPrimitiveTypes.addAll(asList(customPrimitiveTypes));
        return this;
    }

    public ManualRegistry withSerializedObject(final SerializedObjectDefinition serializedObject) {
        if (this.definitions.contains(serializedObject)) {
            throw new UnsupportedOperationException(format("The serializedObject %s has already been added for type %s",
                    serializedObject, serializedObject.type().description()));
        }
        this.definitions.add(serializedObject);
        return this;
    }

    public ManualRegistry withSerializedObject(final Class<?> type,
                                               final Field[] serializedFields,
                                               final String deserializationMethodName) {
        final SerializedObjectDeserializer deserializer = methodNameDeserializer(fullType(type), deserializationMethodName, serializedFields);
        final SerializationFields serializationFields = FIELD_DETECTOR.detect(fullType(type));
        final SerializedObjectSerializer serializer = serializedObjectSerializer(fullType(type), serializationFields);
        final SerializedObjectDefinition serializedObject = serializedObjectDefinition(fullType(type), serializer, deserializer);
        return this.withSerializedObject(serializedObject);
    }

    public ManualRegistry withSerializedObjects(final Class<?>... serializedObjectTypes) {
        stream(serializedObjectTypes).forEach(type -> validateNotNull(type, "type"));
        this.manuallyAddedSerializedObjectTypes.addAll(asList(serializedObjectTypes));
        return this;
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder) {
        this.definitions.forEach(mapMateBuilder::withManuallyAddedDefinition);
        this.manuallyAddedCustomPrimitiveTypes.forEach(mapMateBuilder::withManuallyAddedType);
        this.manuallyAddedSerializedObjectTypes.forEach(mapMateBuilder::withManuallyAddedType);
    }
}
