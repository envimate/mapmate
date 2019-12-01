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

import com.envimate.mapmate.builder.detection.DefinitionFactory;
import com.envimate.mapmate.builder.detection.Detector;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.builder.conventional.ConventionalDefinitionFactories.nameAndConstructorBasedCustomPrimitiveDefinitionFactory;
import static com.envimate.mapmate.builder.detection.SimpleDetector.detector;
import static com.envimate.mapmate.builder.detection.collection.ArrayCollectionDefinitionFactory.arrayFactory;
import static com.envimate.mapmate.builder.detection.collection.NativeJavaCollectionDefinitionFactory.nativeJavaCollectionsFactory;
import static com.envimate.mapmate.builder.detection.customprimitive.BuiltInPrimitivesFactory.builtInPrimitivesFactory;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DetectorBuilder {
    private final List<DefinitionFactory> collectionFactories;
    private final List<DefinitionFactory> customPrimitiveDefinitionFactories;
    private final List<DefinitionFactory> serializedObjectDefinitionFactories;

    public static DetectorBuilder detectorBuilder() {
        final DetectorBuilder detectorBuilder = new DetectorBuilder(new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        detectorBuilder.withCustomPrimitiveFactory(builtInPrimitivesFactory());
        return detectorBuilder;
    }

    public DetectorBuilder withCollectionFactory(final DefinitionFactory collectionFactory) {
        validateNotNull(collectionFactory, "collectionFactory");
        this.collectionFactories.add(collectionFactory);
        return this;
    }

    public DetectorBuilder withNameAndConstructorBasedCustomPrimitiveFactory(final String serializationMethodName,
                                                                             final String deserializationMethodName) {
        validateNotNull(serializationMethodName, "serializationMethodName");
        validateNotNull(deserializationMethodName, "deserializationMethodName");
        final DefinitionFactory factory = nameAndConstructorBasedCustomPrimitiveDefinitionFactory(
                serializationMethodName,
                deserializationMethodName);
        return withCustomPrimitiveFactory(factory);
    }

    public DetectorBuilder withCustomPrimitiveFactory(final DefinitionFactory factory) {
        validateNotNull(factory, "factory");
        this.customPrimitiveDefinitionFactories.add(factory);
        return this;
    }

    public DetectorBuilder withSerializedObjectFactory(final DefinitionFactory factory) {
        validateNotNull(factory, "factory");
        this.serializedObjectDefinitionFactories.add(factory);
        return this;
    }

    public Detector build() {
        withCollectionFactory(arrayFactory());
        withCollectionFactory(nativeJavaCollectionsFactory());
        return detector(this.collectionFactories, this.customPrimitiveDefinitionFactories, this.serializedObjectDefinitionFactories);
    }
}
