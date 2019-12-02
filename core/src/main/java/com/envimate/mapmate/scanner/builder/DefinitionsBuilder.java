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

package com.envimate.mapmate.scanner.builder;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.Definitions;
import com.envimate.mapmate.scanner.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.scanner.builder.detection.Detector;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.mapper.definitions.DefinitionMultiplexer.multiplex;
import static com.envimate.mapmate.mapper.definitions.Definitions.definitions;
import static com.envimate.mapmate.scanner.builder.RequiredCapabilities.all;

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

    private void recurse(final ResolvedType type,
                         final Detector detector,
                         final BuildContextLog contextLog) {
        if (isPresent(type)) {
            return;
        }
        detector.detect(type, all() /* TODO */, contextLog).ifPresent(definition -> {
            contextLog.log(type, "added because it is a dependency");
            addDefinition(definition);
            diveIntoChildren(definition, detector, contextLog.stepInto(type.assignableType()));
        });
    }

    private void diveIntoChildren(final Definition definition, final Detector detector, final BuildContextLog contextLog) {
        multiplex(definition)
                .forSerializedObject(serializedObject -> {
                    serializedObject.serializer().ifPresent(serializer ->
                            serializer.fields().fields()
                                    .forEach(serializationField -> {
                                        final ResolvedType type = serializationField.type();
                                        recurse(type, detector, contextLog);
                                    }));
                    serializedObject.deserializer().ifPresent(deserializer -> {
                        deserializer.fields().referencedTypes()
                                .forEach(referencedType -> recurse(referencedType, detector, contextLog));
                    });
                })
                .forCollection(collection -> {
                    final ResolvedType contentType = collection.contentType();
                    recurse(contentType, detector, contextLog);
                });
    }

    public Definitions build() {
        return definitions(this.contextLog, this.definitions);
    }
}
