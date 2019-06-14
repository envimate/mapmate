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

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.DefinitionNotFoundException.definitionNotFound;

public final class SerializableDefinitions {

    private final List<SerializableCustomPrimitive> customPrimitives;
    private final List<SerializableDataTransferObject> dataTransferObjects;

    private SerializableDefinitions(final List<SerializableCustomPrimitive> customPrimitives,
                                    final List<SerializableDataTransferObject> dataTransferObjects) {
        this.customPrimitives = customPrimitives;
        this.dataTransferObjects = dataTransferObjects;
    }

    public static SerializableDefinitions serializableDefinitions(
            final List<SerializableCustomPrimitive> customPrimitives,
            final List<SerializableDataTransferObject> dataTransferObjects
    ) {
        return new SerializableDefinitions(customPrimitives, dataTransferObjects);
    }

    public static SerializableDefinitions empty() {
        return new SerializableDefinitions(new LinkedList<>(), new LinkedList<>());
    }

    public static SerializableDefinitions withTheCustomPrimitives(
            final List<SerializableCustomPrimitive> customPrimitives) {
        return new SerializableDefinitions(customPrimitives, new LinkedList<>());
    }

    public static SerializableDefinitions withTheDataTransferObjects(
            final List<SerializableDataTransferObject> dataTransferObjects) {
        return new SerializableDefinitions(new LinkedList<>(), dataTransferObjects);
    }

    public static SerializableDefinitions withASingleCustomPrimitive(
            final SerializableCustomPrimitive customPrimitive
    ) {
        final List<SerializableCustomPrimitive> customPrimitives = new LinkedList<>();
        customPrimitives.add(customPrimitive);
        return withTheCustomPrimitives(customPrimitives);
    }

    public static SerializableDefinitions withASingleDataTransferObject(
            final SerializableDataTransferObject dataTransferObject
    ) {
        final List<SerializableDataTransferObject> dataTransferObjects = new LinkedList<>();
        dataTransferObjects.add(dataTransferObject);
        return withTheDataTransferObjects(dataTransferObjects);
    }

    public static SerializableDefinitions merge(final SerializableDefinitions a,
                                                final SerializableDefinitions b) {
        final List<SerializableCustomPrimitive> customPrimitives = new LinkedList<>();
        customPrimitives.addAll(a.customPrimitives);
        customPrimitives.addAll(b.customPrimitives);
        final List<SerializableDataTransferObject> dataTransferObjects = new LinkedList<>();
        dataTransferObjects.addAll(a.dataTransferObjects);
        dataTransferObjects.addAll(b.dataTransferObjects);
        return new SerializableDefinitions(customPrimitives, dataTransferObjects);
    }

    public Definition getDefinitionForObject(final Object object) {
        final Class<?> targetType = object.getClass();
        return this.getDefinitionForType(targetType);
    }

    public Definition getDefinitionForType(final Class<?> targetType) {
        final List<Definition> allDefinitions = new LinkedList<>();
        allDefinitions.addAll(this.customPrimitives);
        allDefinitions.addAll(this.dataTransferObjects);
        return allDefinitions.stream()
                .filter(e -> e.getType() == targetType)
                .findAny()
                .orElseThrow(() -> definitionNotFound(targetType));
    }

    public int countCustomPrimitives() {
        return this.customPrimitives.size();
    }

    public int countDataTransferObjects() {
        return this.dataTransferObjects.size();
    }
}
