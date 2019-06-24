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

import com.envimate.mapmate.builder.Detector;
import com.envimate.mapmate.builder.MapMateBuilder;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.recipes.Recipe;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.builder.definitions.SerializedObjectDefinition.serializedObjectDefinition;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManualRegistry implements Recipe {
    private final Map<Class<?>, CustomPrimitiveDefinition> customPrimitiveDefinitions = new HashMap<>(1);
    private final Map<Class<?>, SerializedObjectDefinition> serializedObjectDefinitions = new HashMap<>(1);
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
        final CustomPrimitiveDefinition alreadyAdded = this.customPrimitiveDefinitions.put(
                customPrimitive.type,
                customPrimitive);
        if (alreadyAdded != null) {
            throw new UnsupportedOperationException(String.format(
                    "The customPrimitive %s has already been added for type %s and is %s",
                    customPrimitive,
                    customPrimitive.type,
                    alreadyAdded));
        }

        return this;
    }

    public ManualRegistry withCustomPrimitives(final Class<?>... customPrimitiveTypes) {
        this.manuallyAddedCustomPrimitiveTypes.addAll(Arrays.asList(customPrimitiveTypes));
        return this;
    }

    public ManualRegistry withSerializedObject(final SerializedObjectDefinition serializedObject) {
        final SerializedObjectDefinition alreadyAdded = this.serializedObjectDefinitions.put(serializedObject.type,
                serializedObject);
        if (alreadyAdded != null) {
            throw new UnsupportedOperationException(String.format(
                    "The serializedObject %s has already been added for type %s and is %s",
                    serializedObject,
                    serializedObject.type,
                    alreadyAdded));
        }
        return this;
    }

    public ManualRegistry withSerializedObject(final Class<?> type,
                                               final Field[] serializedFields,
                                               final String deserializationMethodName) {
        return this.withSerializedObject(serializedObjectDefinition(type, serializedFields, deserializationMethodName));
    }

    public ManualRegistry withSerializedObjects(final Class<?>... serializedObjectTypes) {
        this.manuallyAddedSerializedObjectTypes.addAll(Arrays.asList(serializedObjectTypes));
        return this;
    }

    @Override
    public Map<Class<?>, CustomPrimitiveDefinition> customPrimitiveDefinitions() {
        return Collections.unmodifiableMap(this.customPrimitiveDefinitions);
    }

    @Override
    public Map<Class<?>, SerializedObjectDefinition> serializedObjectDefinitions() {
        return Collections.unmodifiableMap(this.serializedObjectDefinitions);
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder) {
        final Detector detector = mapMateBuilder.detector;

        final List<CustomPrimitiveDefinition> customPrimitives = detector
                .customPrimitives(this.manuallyAddedCustomPrimitiveTypes);
        customPrimitives.forEach(customPrimitiveDefinition ->
                this.customPrimitiveDefinitions.put(customPrimitiveDefinition.type, customPrimitiveDefinition)
        );

        final List<SerializedObjectDefinition> serializedObjects = detector
                .serializedObjects(this.manuallyAddedSerializedObjectTypes);
        serializedObjects.forEach(serializedObjectDefinition ->
                this.serializedObjectDefinitions.put(serializedObjectDefinition.type, serializedObjectDefinition)
        );
    }
}
