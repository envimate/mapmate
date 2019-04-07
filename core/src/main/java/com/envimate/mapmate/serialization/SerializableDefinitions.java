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

import java.util.HashSet;
import java.util.Set;

import static com.envimate.mapmate.DefinitionNotFoundException.definitionNotFound;

public final class SerializableDefinitions {

    private final Set<SerializableCustomPrimitive> customPrimitives;
    private final Set<SerializableDataTransferObject> dataTransferObjects;

    private SerializableDefinitions(final Set<SerializableCustomPrimitive> customPrimitives,
                                    final Set<SerializableDataTransferObject> dataTransferObjects) {
        this.customPrimitives = customPrimitives;
        this.dataTransferObjects = dataTransferObjects;
    }

    public static SerializableDefinitions empty() {
        return new SerializableDefinitions(new HashSet<>(0), new HashSet<>(0));
    }

    public static SerializableDefinitions withTheCustomPrimitives(
            final Set<SerializableCustomPrimitive> customPrimitives) {
        return new SerializableDefinitions(customPrimitives, new HashSet<>(0));
    }

    public static SerializableDefinitions withTheDataTransferObjects(
            final Set<SerializableDataTransferObject> dataTransferObjects) {
        return new SerializableDefinitions(new HashSet<>(0), dataTransferObjects);
    }

    public static SerializableDefinitions withASingleCustomPrimitive(final SerializableCustomPrimitive customPrimitive) {
        final Set<SerializableCustomPrimitive> customPrimitives = new HashSet<>(1);
        customPrimitives.add(customPrimitive);
        return withTheCustomPrimitives(customPrimitives);
    }

    public static SerializableDefinitions withASingleDataTransferObject(final SerializableDataTransferObject dataTransferObject) {
        final Set<SerializableDataTransferObject> dataTransferObjects = new HashSet<>(1);
        dataTransferObjects.add(dataTransferObject);
        return withTheDataTransferObjects(dataTransferObjects);
    }

    public static SerializableDefinitions merge(final SerializableDefinitions a,
                                                final SerializableDefinitions b) {
        final Set<SerializableCustomPrimitive> customPrimitives = new HashSet<>();
        customPrimitives.addAll(a.customPrimitives);
        customPrimitives.addAll(b.customPrimitives);
        final Set<SerializableDataTransferObject> dataTransferObjects = new HashSet<>();
        dataTransferObjects.addAll(a.dataTransferObjects);
        dataTransferObjects.addAll(b.dataTransferObjects);
        return new SerializableDefinitions(customPrimitives, dataTransferObjects);
    }

    public Definition getDefinitionForObject(final Object object) {
        final Class<?> targetType = object.getClass();
        return getDefinitionForType(targetType);
    }

    public Definition getDefinitionForType(final Class<?> targetType) {
        final Set<Definition> allDefinitions = new HashSet<>();
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
