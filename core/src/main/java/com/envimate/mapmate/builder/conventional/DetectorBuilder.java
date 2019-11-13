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
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static com.envimate.mapmate.builder.conventional.SimpleDetector.detector;
import static com.envimate.mapmate.builder.conventional.DefinitionFactories.*;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DetectorBuilder {
    private final List<CustomPrimitiveDefinitionFactory> customPrimitiveDefinitionFactories;
    private final List<SerializedObjectDefinitionFactory> serializedObjectDefinitionFactories;

    public static DetectorBuilder detectorBuilder() {
        return new DetectorBuilder(new LinkedList<>(), new LinkedList<>());
    }

    public DetectorBuilder withNameAndConstructorBasedCustomPrimitiveFactory(final String serializationMethodName,
                                                                             final String deserializationMethodName) {
        validateNotNull(serializationMethodName, "serializationMethodName");
        validateNotNull(deserializationMethodName, "deserializationMethodName");
        final CustomPrimitiveDefinitionFactory factory = nameAndConstructorBasedCustomPrimitiveDefinitionFactory(
                serializationMethodName,
                deserializationMethodName);
        return withCustomPrimitiveFactory(factory);
    }

    public DetectorBuilder withCustomPrimitiveFactory(final CustomPrimitiveDefinitionFactory factory) {
        validateNotNull(factory, "factory");
        this.customPrimitiveDefinitionFactories.add(factory);
        return this;
    }

    public DetectorBuilder withMethodNameBasedSerializedObjectFactory(final String deserializationMethodName) {
        validateNotNull(deserializationMethodName, "deserializationMethodName");
        final SerializedObjectDefinitionFactory factory =
                deserializerMethodNameBasedSerializedObjectFactory(deserializationMethodName);
        return withSerializedObjectFactory(factory);
    }

    public DetectorBuilder withClassNameBasedSerializedObjectFactory(final String deserializationMethodName,
                                                                     final String... classPatterns) {
        validateNotNull(deserializationMethodName, "deserializationMethodName");
        validateNotNull(classPatterns, "classPatterns");
        final List<Pattern> patterns = stream(classPatterns)
                .map(Pattern::compile)
                .collect(toList());
        final SerializedObjectDefinitionFactory factory = nameAndConstructorBasedSerializedObjectFactory(patterns,
                deserializationMethodName);
        return withSerializedObjectFactory(factory);
    }

    public DetectorBuilder withSerializedObjectFactory(final SerializedObjectDefinitionFactory factory) {
        validateNotNull(factory, "factory");
        this.serializedObjectDefinitionFactories.add(factory);
        return this;
    }

    public Detector build() {
        return detector(this.customPrimitiveDefinitionFactories, this.serializedObjectDefinitionFactories);
    }
}
