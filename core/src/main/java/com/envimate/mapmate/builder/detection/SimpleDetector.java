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

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.builder.RequiredCapabilities;
import com.envimate.mapmate.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.util.Optional.empty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleDetector implements Detector {
    private final List<DefinitionFactory> collectionDefinitionFactories;
    private final List<DefinitionFactory> customPrimitiveDefinitionFactories;
    private final List<DefinitionFactory> serializedObjectDefinitionFactories;

    public static Detector detector(final List<DefinitionFactory> collectionDefinitionFactories,
                                    final List<DefinitionFactory> customPrimitiveDefinitionFactories,
                                    final List<DefinitionFactory> serializedObjectDefinitionFactories) {
        validateNotNull(collectionDefinitionFactories, "collectionDefinitionFactories");
        validateNotNull(customPrimitiveDefinitionFactories, "customPrimitiveDefinitionFactories");
        validateNotNull(serializedObjectDefinitionFactories, "serializedObjectDefinitionFactories");
        return new SimpleDetector(collectionDefinitionFactories, customPrimitiveDefinitionFactories, serializedObjectDefinitionFactories);
    }

    @Override
    public Optional<? extends Definition> detect(final ResolvedType type,
                                                 final RequiredCapabilities capabilities,
                                                 final BuildContextLog parentLog) {
        final BuildContextLog contextLog = parentLog.stepInto(SimpleDetector.class);
        final Optional<? extends Definition> collection = detectCollectionDefinition(type, capabilities, contextLog);
        if (collection.isPresent()) {
            return collection;
        }

        final Optional<? extends Definition> customPrimitive = detectCustomPrimitive(type, capabilities, contextLog);
        if (customPrimitive.isPresent()) {
            return customPrimitive;
        }
        return detectSerializedObject(type, capabilities, contextLog);
    }

    private Optional<Definition> detectCollectionDefinition(final ResolvedType type,
                                                            final RequiredCapabilities capabilities,
                                                            final BuildContextLog contextLog) {
        return detectIn(type, capabilities, this.collectionDefinitionFactories, contextLog);
    }

    private Optional<Definition> detectCustomPrimitive(final ResolvedType type,
                                                       final RequiredCapabilities capabilities,
                                                       final BuildContextLog contextLog) {
        return detectIn(type, capabilities, this.customPrimitiveDefinitionFactories, contextLog);
    }

    private Optional<Definition> detectSerializedObject(final ResolvedType type,
                                                        final RequiredCapabilities capabilities,
                                                        final BuildContextLog contextLog) {
        return detectIn(type, capabilities, this.serializedObjectDefinitionFactories, contextLog);
    }

    private static Optional<Definition> detectIn(final ResolvedType type,
                                                 final RequiredCapabilities capabilities,
                                                 final List<DefinitionFactory> factories,
                                                 final BuildContextLog contextLog) {
        if (!isSupported(type)) {
            contextLog.logReject(type, "type is not supported because it contains wildcard generics (\"?\")");
            return empty();
        }
        for (final DefinitionFactory factory : factories) {
            final BuildContextLog factoryContextLog = contextLog.stepInto(factory.getClass());
            final Optional<Definition> analyzedClass = factory.analyze(type, capabilities);
            if (analyzedClass.isPresent()) {
                factoryContextLog.log(type, "know how to handle this type");
                return analyzedClass;
            } else {
                factoryContextLog.logReject(type, "do not know how to handle this type");
            }
        }
        return empty();
    }

    private static boolean isSupported(final ResolvedType resolvedType) {
        if(resolvedType.isWildcard()) {
            return false;
        }
        return resolvedType.typeParameters().stream()
                .allMatch(SimpleDetector::isSupported);
    }
}
