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

package com.envimate.mapmate.definitions;

import com.envimate.mapmate.builder.DefinitionSeed;
import com.envimate.mapmate.builder.DefinitionSeeds;
import com.envimate.mapmate.builder.RequiredCapabilities;
import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.definitions.types.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.builder.DefinitionSeeds.definitionSeeds;
import static com.envimate.mapmate.builder.RequiredCapabilities.*;
import static com.envimate.mapmate.definitions.DefinitionMultiplexer.multiplex;
import static com.envimate.mapmate.definitions.Definitions.definitions;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionsBuilder {
    private static final int INITIAL_CAPACITY = 10000;

    private final Map<FullType, Definition> definitions = new HashMap<>(INITIAL_CAPACITY);
    private final Detector detector;

    public static DefinitionsBuilder definitionsBuilder(final Detector detector) {
        return new DefinitionsBuilder(detector);
    }

    public void detectAndAdd(final DefinitionSeed seed) {
        if (isPresent(seed.fullType())) {
            return;
        }
        this.detector.detect(seed.fullType(), seed.requiredCapabilities()).ifPresent(this::addDefinition);
    }

    public void addDefinition(final Definition definition) {
        this.definitions.put(definition.type(), definition);
    }

    private boolean isPresent(final FullType detectionCandidate) {
        return this.definitions.containsKey(detectionCandidate);
    }

    public void resolveRecursively(final Detector detector) {
        final List<Definition> seedDefinitions = new LinkedList<>(this.definitions.values());
        seedDefinitions.forEach(definition -> diveIntoChildren(definition, detector));
    }

    private void recurse(final FullType type, final Detector detector, final RequiredCapabilities capabilities) {
        if (isPresent(type)) {
            return;
        }
        detector.detect(type, capabilities).ifPresent(definition -> {
            addDefinition(definition);
            diveIntoChildren(definition, detector);
        });
    }

    private void diveIntoChildren(final Definition definition, final Detector detector) {
        multiplex(definition)
                .forSerializedObject(serializedObject -> {
                    final DefinitionSeeds seeds = definitionSeeds();
                    serializedObject.serializer().ifPresent(serializer ->
                            serializer.fields().fields().forEach(field -> seeds.add(field.type(), serializationOnly())));
                    serializedObject.deserializer().ifPresent(deserializer ->
                            deserializer.fields().referencedTypes().forEach(referencedType -> seeds.add(referencedType, deserializationOnly())));
                    seeds.types().forEach(fullType -> recurse(fullType, detector, seeds.capabilitiesFor(fullType)));
                })
                .forCollection(collection -> recurse(collection.contentType(), detector, all()));
    }

    public Definitions build(final DefinitionSeeds seeds) {
        return definitions(this.definitions, seeds);
    }
}
