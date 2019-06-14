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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.deserialization.UnknownReferenceException.fromType;
import static com.envimate.mapmate.deserialization.methods.DeserializationMethodNotCompatibleException.deserializationMethodNotCompatibleException;

public final class DeserializableDefinitions {

    private final List<DeserializableCustomPrimitive<?>> customPrimitives;
    private final List<DeserializableDataTransferObject<?>> dataTransferObjects;

    private DeserializableDefinitions(final List<DeserializableCustomPrimitive<?>> customPrimitives,
                                      final List<DeserializableDataTransferObject<?>> dataTransferObjects) {
        this.customPrimitives = customPrimitives;
        this.dataTransferObjects = dataTransferObjects;
    }

    public static DeserializableDefinitions deserializableDefinitions(
            final List<DeserializableCustomPrimitive<?>> customPrimitives,
            final List<DeserializableDataTransferObject<?>> dataTransferObjects) {

        return new DeserializableDefinitions(customPrimitives, dataTransferObjects);
    }

    public static DeserializableDefinitions empty() {
        return new DeserializableDefinitions(new LinkedList<>(), new LinkedList<>());
    }

    public static DeserializableDefinitions withTheCustomPrimitives(
            final List<DeserializableCustomPrimitive<?>> customPrimitives) {
        return new DeserializableDefinitions(customPrimitives, new LinkedList<>());
    }

    public static DeserializableDefinitions withTheDataTransferObjects(
            final List<DeserializableDataTransferObject<?>> dataTransferObjects) {
        return new DeserializableDefinitions(new LinkedList<>(), dataTransferObjects);
    }

    public static DeserializableDefinitions withASingleCustomPrimitive(
            final DeserializableCustomPrimitive<?> customPrimitive) {
        final List<DeserializableCustomPrimitive<?>> customPrimitives = new LinkedList<>();
        customPrimitives.add(customPrimitive);
        return withTheCustomPrimitives(customPrimitives);
    }

    public static DeserializableDefinitions withASingleDataTransferObject(
            final DeserializableDataTransferObject<?> dataTransferObject) {
        final List<DeserializableDataTransferObject<?>> dataTransferObjects = new LinkedList<>();
        dataTransferObjects.add(dataTransferObject);
        return withTheDataTransferObjects(dataTransferObjects);
    }

    public static DeserializableDefinitions theSpeciallyTreatedCustomPrimitives() {
        final List<DeserializableCustomPrimitive<?>> speciallyTreatedCustomPrimitives = new LinkedList<>();
        speciallyTreatedCustomPrimitives.add(deserializableCustomPrimitive(String.class, new DeserializationCPMethod() {
            @Override
            public void verifyCompatibility(final Class<?> targetType) {
                if (targetType != String.class) {
                    throw deserializationMethodNotCompatibleException("can only deserialize strings");
                }
            }

            @Override
            public Object deserialize(final String input, final Class<?> targetType) throws Exception {
                return input;
            }
        }));
        return new DeserializableDefinitions(speciallyTreatedCustomPrimitives, new LinkedList<>());
    }

    public static DeserializableDefinitions merge(final DeserializableDefinitions a,
                                                  final DeserializableDefinitions b) {
        final List<DeserializableCustomPrimitive<?>> customPrimitives = new LinkedList<>();
        customPrimitives.addAll(a.customPrimitives);
        customPrimitives.addAll(b.customPrimitives);
        final List<DeserializableDataTransferObject<?>> dataTransferObjects = new LinkedList<>();
        dataTransferObjects.addAll(a.dataTransferObjects);
        dataTransferObjects.addAll(b.dataTransferObjects);
        return new DeserializableDefinitions(customPrimitives, dataTransferObjects);
    }

    public int countCustomPrimitives() {
        return this.customPrimitives.size();
    }

    public int countDataTransferObjects() {
        return this.dataTransferObjects.size();
    }

    public Optional<Definition> getDefinitionForType(final Class<?> targetType) {
        final List<Definition> allDefinitions = new LinkedList<>();
        allDefinitions.addAll(this.customPrimitives);
        allDefinitions.addAll(this.dataTransferObjects);
        return allDefinitions.stream()
                .filter(e -> e.getType() == targetType)
                .findAny();
    }

    public void validateNoUnsupportedOutgoingReferences() {
        final List<Class<?>> references = this.allReferences();
        for (final Class<?> reference : references) {
            if (this.getDefinitionForType(reference).isEmpty()) {
                throw fromType(reference);
            }
        }
    }

    private List<Class<?>> allReferences() {
        final List<Class<?>> allReferences = new LinkedList<>();
        for (final DeserializableDataTransferObject<?> dataTransferObject : this.dataTransferObjects) {
            final List<Class<?>> references = dataTransferObject.elements().referencedTypes();
            allReferences.addAll(references);
        }
        return allReferences;
    }
}
