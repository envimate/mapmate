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
import com.envimate.mapmate.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.definitions.types.ClassType;
import com.envimate.mapmate.definitions.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.definitions.DefinitionMultiplexer.multiplex;
import static com.envimate.mapmate.definitions.Definitions.definitions;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionsBuilder {
    private static final int INITIAL_CAPACITY = 10000;

    private final Map<ResolvedType, Definition> definitions = new HashMap<>(INITIAL_CAPACITY);
    private final BuildContextLog contextLog;
    private final Detector detector;

    public static DefinitionsBuilder definitionsBuilder(final Detector detector,
                                                        final BuildContextLog contextLog) {
        return new DefinitionsBuilder(contextLog.stepInto(DefinitionsBuilder.class), detector);
    }

    public void detectAndAdd(final DefinitionSeed seed) {
        if (isPresent(seed.type())) {
            return;
        }
        this.detector.detect(seed, this.contextLog).ifPresent(this::addDefinition);
    }

    public void addDefinition(final Definition definition) {
        this.definitions.put(definition.type(), definition);
    }

    private boolean isPresent(final ResolvedType detectionCandidate) {
        return this.definitions.containsKey(detectionCandidate);
    }

    public void resolveRecursively(final Detector detector) {
        final List<Definition> seedDefinitions = new LinkedList<>(this.definitions.values());
        seedDefinitions.forEach(definition -> diveIntoChildren(definition, detector, this.contextLog.stepInto(definition.type().assignableType())));
    }

    private void recurse(final DefinitionSeed seed, final Detector detector, final BuildContextLog contextLog) {
        if (isPresent(seed.type())) {
            return;
        }
        detector.detect(seed, contextLog).ifPresent(definition -> {
            contextLog.log(seed.type(), "added because it is a dependency");
            addDefinition(definition);
            diveIntoChildren(definition, detector, contextLog.stepInto(seed.type().assignableType()));
        });
    }

    private void diveIntoChildren(final Definition definition, final Detector detector, final BuildContextLog contextLog) {
        multiplex(definition)
                .forSerializedObject(serializedObject -> {
                    serializedObject.serializer().ifPresent(serializer ->
                            serializer.fields().fields()
                                    .forEach(serializationField -> {
                                        final ResolvedType type = serializationField.type();
                                        recurse(definition.context().childForType(type), detector, contextLog);
                                    }));
                    serializedObject.deserializer().ifPresent(deserializer -> {
                        deserializer.fields().referencedTypes()
                                .forEach(referencedType -> {
                                    recurse(definition.context().childForType(referencedType), detector, contextLog);
                                });
                    });
                })
                .forCollection(collection -> {
                    final ResolvedType contentType = collection.contentType();
                    recurse(definition.context().childForType(contentType), detector, contextLog);
                });
    }

    public Definitions build(final DefinitionSeeds seeds) {
        return definitions(this.contextLog, this.definitions, seeds);
    }
}
