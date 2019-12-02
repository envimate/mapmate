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

import com.envimate.mapmate.builder.DefinitionSeed;
import com.envimate.mapmate.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
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
    public Optional<? extends Definition> detect(final DefinitionSeed context,
                                                 final BuildContextLog parentLog) {
        final BuildContextLog contextLog = parentLog.stepInto(SimpleDetector.class);
        final Optional<? extends Definition> collection = detectCollectionDefinition(context, contextLog);
        if (collection.isPresent()) {
            return collection;
        }

        final Optional<? extends Definition> customPrimitive = detectCustomPrimitive(context, contextLog);
        if (customPrimitive.isPresent()) {
            return customPrimitive;
        }
        return detectSerializedObject(context, contextLog);
    }

    private Optional<Definition> detectCollectionDefinition(final DefinitionSeed context,
                                                            final BuildContextLog contextLog) {
        return detectIn(context, this.collectionDefinitionFactories, contextLog);
    }

    private Optional<Definition> detectCustomPrimitive(final DefinitionSeed context,
                                                       final BuildContextLog contextLog) {
        return detectIn(context, this.customPrimitiveDefinitionFactories, contextLog);
    }

    private Optional<Definition> detectSerializedObject(final DefinitionSeed context,
                                                        final BuildContextLog contextLog) {
        return detectIn(context, this.serializedObjectDefinitionFactories, contextLog);
    }

    private static Optional<Definition> detectIn(final DefinitionSeed context,
                                                 final List<DefinitionFactory> factories,
                                                 final BuildContextLog contextLog) {
        final ResolvedType type = context.type();
        if (!isSupported(type)) {
            contextLog.logReject(type, "type is not supported because it contains wildcard generics (\"?\")");
            return empty();
        }
        for (final DefinitionFactory factory : factories) {
            final Optional<Definition> analyzedClass = factory.analyze(context, type, context.requiredCapabilities());
            if (analyzedClass.isPresent()) {
                return analyzedClass;
            }
        }
        contextLog.logReject(type, "do not know how to handle this type");
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
