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

package com.envimate.mapmate.builder.detection;

import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.builder.detection.customprimitive.CustomPrimitiveDefinitionFactory;
import com.envimate.mapmate.builder.detection.serializedobject.SerializedObjectDefinitionFactory;
import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.SerializedObjectDefinition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Optional.empty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleDetector implements Detector {
    private final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories;
    private final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories;

    public static Detector detector(final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories,
                                    final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories) {
        validateNotNull(customPrimitiveDefinitionFactories, "customPrimitiveDefinitionFactories");
        validateNotNull(serializedObjectDefinitionFactories, "serializedObjectDefinitionFactories");
        return new SimpleDetector(customPrimitiveDefinitionFactories, serializedObjectDefinitionFactories);
    }

    @Override
    public Optional<? extends Definition> detect(final Class<?> type) {
        if (isAbstract(type.getModifiers())) {
            return empty();
        }
        final Optional<? extends Definition> customPrimitive = detectCustomPrimitive(type);
        if(customPrimitive.isPresent()) {
            return customPrimitive;
        }
        return detectSerializedObject(type);
    }

    private Optional<CustomPrimitiveDefinition> detectCustomPrimitive(final Class<?> type) {
        for (final CustomPrimitiveDefinitionFactory factory : this.customPrimitiveDefinitionFactories) {
            final Optional<CustomPrimitiveDefinition> analyzedClass = factory.analyze(type);
            if (analyzedClass.isPresent()) {
                return analyzedClass;
            }
        }
        return empty();
    }

    private Optional<SerializedObjectDefinition> detectSerializedObject(final Class<?> type) {
        for (final SerializedObjectDefinitionFactory factory : this.serializedObjectDefinitionFactories) {
            final Optional<SerializedObjectDefinition> analyzedClass = factory.analyze(type);
            if (analyzedClass.isPresent()) {
                return analyzedClass;
            }
        }
        return empty();
    }
}
