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

package com.envimate.mapmate.builder.recipes.recursive;

import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.MapMateBuilder;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.SerializedObjectDefinition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.mapmate.builder.recipes.recursive.ScanResultBuilder.scanResultBuilder;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecursiveRecipe implements Recipe {
    private final Class<?> type;
    private final ScanResultBuilder scanResultBuilder = scanResultBuilder();

    public static Recipe addWithAllDependencies(final Class<?> type) {
        validateNotNull(type, "type");
        return new RecursiveRecipe(type);
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder) {
        final Detector detector = mapMateBuilder.detector;
        recurse(this.type, detector);
    }

    @Override
    public List<CustomPrimitiveDefinition> customPrimitiveDefinitions() {
        return this.scanResultBuilder.customPrimitiveDefinitions();
    }

    @Override
    public List<SerializedObjectDefinition> serializedObjectDefinitions() {
        return this.scanResultBuilder.serializedObjectDefinitions();
    }

    @SuppressWarnings("CastToConcreteClass")
    private void recurse(final Class<?> type, final Detector detector) {
        if (this.scanResultBuilder.alreadyHas(type)) {
            return;
        }
        detector.detect(type).ifPresent(definition -> {
            if (definition.isCustomPrimitive()) {
                this.scanResultBuilder.addCustomPrimitive((CustomPrimitiveDefinition) definition);
            } else {
                final SerializedObjectDefinition serializedObject = (SerializedObjectDefinition) definition;
                this.scanResultBuilder.addSerializedObject(serializedObject);
                serializedObject.serializer().fields().fields().forEach(field -> recurse(field.type(), detector));
                serializedObject.deserializer().fields().referencedTypes().forEach(referencedType -> recurse(referencedType, detector));
            }
        });
    }
}
