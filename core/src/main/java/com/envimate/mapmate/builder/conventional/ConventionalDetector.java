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

package com.envimate.mapmate.builder.conventional;

import com.envimate.mapmate.builder.Detector;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.conventional.customprimitives.classannotation.CustomPrimitiveClassAnnotationFactory.customPrimitiveClassAnnotationFactory;
import static com.envimate.mapmate.builder.conventional.customprimitives.methodannotation.CustomPrimitiveMethodAnnotationFactory.customPrimitiveMethodAnnotationFactory;
import static com.envimate.mapmate.builder.conventional.customprimitives.namebased.MethodNameBasedCustomPrimitiveDefinitionFactory.nameBasedCustomPrimitiveDefinitionFactory;
import static com.envimate.mapmate.builder.conventional.serializedobject.classannotation.SerializedObjectClassAnnotationFactory.serializedObjectClassAnnotationFactory;
import static com.envimate.mapmate.builder.conventional.serializedobject.namebased.DeserializerMethodNameBasedSerializedObjectFactory.deserializerMethodNameBasedSerializedObjectFactory;
import static com.envimate.mapmate.builder.conventional.serializedobject.namebased.NameBasedSerializedObjectFactory.nameBasedSerializedObjectFactory;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConventionalDetector implements Detector {
    private final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories;
    private final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories;

    public static ConventionalDetector conventionalDetector() {
        return conventionalDetector("stringValue",
                "fromStringValue",
                "deserialize",
                ".*DTO",
                ".*Dto",
                ".*Request",
                ".*Response",
                ".*State"
        );
    }

    public static ConventionalDetector conventionalDetector(final String customPrimitiveSerializationMethodName,
                                                            final String customPrimitiveDeserializationMethodName,
                                                            final String serializedObjectDeserializationMethodName,
                                                            final String... serializedObjectNameDetectionPatterns
    ) {
        validateNotNull(customPrimitiveSerializationMethodName, "customPrimitiveSerializationMethodName");
        validateNotNull(customPrimitiveDeserializationMethodName, "customPrimitiveDeserializationMethodName");
        validateNotNull(serializedObjectDeserializationMethodName, "serializedObjectDeserializationMethodName");
        validateNotNull(serializedObjectNameDetectionPatterns, "serializedObjectNameDetectionPatterns");

        final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories = List.of(
                nameBasedCustomPrimitiveDefinitionFactory(
                        customPrimitiveSerializationMethodName,
                        customPrimitiveDeserializationMethodName)
        );

        final List<Pattern> patterns = Arrays.stream(serializedObjectNameDetectionPatterns)
                .map(Pattern::compile)
                .collect(Collectors.toList());
        final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories = List.of(
                deserializerMethodNameBasedSerializedObjectFactory(serializedObjectDeserializationMethodName),
                nameBasedSerializedObjectFactory(
                        patterns,
                        serializedObjectDeserializationMethodName));
        return conventionalDetector(customPrimitiveDefinitionFactories, serializedObjectDefinitionFactories);
    }

    public static ConventionalDetector conventionalDetector(
            final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories,
            final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories
    ) {
        validateNotNull(customPrimitiveDefinitionFactories, "customPrimitiveDefinitionFactories");
        validateNotNull(serializedObjectDefinitionFactories, "serializedObjectDefinitionFactories");
        return new ConventionalDetector(customPrimitiveDefinitionFactories, serializedObjectDefinitionFactories);
    }

    public static ConventionalDetector conventionalDetectorWithAnnotations() {
        return conventionalDetectorWithAnnotations("stringValue",
                "fromStringValue",
                "deserialize",
                ".*DTO",
                ".*Dto",
                ".*Request",
                ".*Response",
                ".*State"
        );
    }

    public static ConventionalDetector conventionalDetectorWithAnnotations(
            final String customPrimitiveSerializationMethodName,
            final String customPrimitiveDeserializationMethodName,
            final String serializedObjectDeserializationMethodName,
            final String... serializedObjectNameDetectionPatterns
    ) {
        validateNotNull(customPrimitiveSerializationMethodName, "customPrimitiveSerializationMethodName");
        validateNotNull(customPrimitiveDeserializationMethodName, "customPrimitiveDeserializationMethodName");
        validateNotNull(serializedObjectDeserializationMethodName, "serializedObjectDeserializationMethodName");
        validateNotNull(serializedObjectNameDetectionPatterns, "serializedObjectNameDetectionPatterns");

        final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories = List.of(
                customPrimitiveClassAnnotationFactory(),
                customPrimitiveMethodAnnotationFactory(),
                nameBasedCustomPrimitiveDefinitionFactory(
                        customPrimitiveSerializationMethodName,
                        customPrimitiveDeserializationMethodName)
        );

        final List<Pattern> patterns = Arrays.stream(serializedObjectNameDetectionPatterns)
                .map(Pattern::compile)
                .collect(Collectors.toList());
        final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories = List.of(
                serializedObjectClassAnnotationFactory(),
                deserializerMethodNameBasedSerializedObjectFactory(serializedObjectDeserializationMethodName),
                nameBasedSerializedObjectFactory(
                        patterns,
                        serializedObjectDeserializationMethodName));
        return conventionalDetector(customPrimitiveDefinitionFactories, serializedObjectDefinitionFactories);
    }

    @Override
    public List<CustomPrimitiveDefinition> customPrimitives(final List<Class<?>> classes) {
        final List<CustomPrimitiveDefinition> foundCustomPrimitives = new LinkedList<>();

        for (final Class<?> scannedClass : classes) {
            for (final CustomPrimitiveDefinitionFactory factory : this.customPrimitiveDefinitionFactories) {
                final Optional<CustomPrimitiveDefinition> analyzedClass = factory.analyze(scannedClass);
                if (analyzedClass.isPresent()) {
                    foundCustomPrimitives.add(analyzedClass.get());
                    break;
                }
            }
        }
        return foundCustomPrimitives;
    }

    @Override
    public List<SerializedObjectDefinition> serializedObjects(final List<Class<?>> classes) {
        final List<SerializedObjectDefinition> foundSerializedObjects = new LinkedList<>();
        for (final Class<?> scannedClass : classes) {
            for (final SerializedObjectDefinitionFactory factory : this.serializedObjectDefinitionFactories) {
                final Optional<SerializedObjectDefinition> analyzedClass = factory.analyze(scannedClass);
                if (analyzedClass.isPresent()) {
                    foundSerializedObjects.add(analyzedClass.get());
                    break;
                }
            }
        }
        return foundSerializedObjects;
    }
}
