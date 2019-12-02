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

package com.envimate.mapmate.builder.recipes.scanner;

import com.envimate.mapmate.MapMateBuilder;
import com.envimate.mapmate.builder.DefinitionSeed;
import com.envimate.mapmate.builder.DependencyRegistry;
import com.envimate.mapmate.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.types.ClassType;
import com.envimate.types.ResolvedType;
import com.envimate.types.resolver.ResolvedMethod;
import com.envimate.types.resolver.ResolvedParameter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.RequiredCapabilities.deserializationOnly;
import static com.envimate.mapmate.builder.RequiredCapabilities.serializationOnly;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static com.envimate.types.ClassType.fromClassWithoutGenerics;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassScannerRecipe implements Recipe {
    private static final List<String> OBJECT_METHODS = stream(Object.class.getMethods())
            .map(Method::getName)
            .collect(toList());

    private final List<Class<?>> classes;

    public static ClassScannerRecipe addAllReferencedClassesIs(final Class<?>... classes) {
        validateNotNull(classes, "classes");
        return new ClassScannerRecipe(asList(classes));
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder, final DependencyRegistry dependencyRegistry) {
        final Detector detector = dependencyRegistry.getDependency(Detector.class);
        this.classes.forEach(clazz -> addReferencesIn(clazz, mapMateBuilder, detector));
    }

    private static void addReferencesIn(final Class<?> clazz, final MapMateBuilder builder, final Detector detector) {
        final ClassType fullType = fromClassWithoutGenerics(clazz);
        final BuildContextLog contextLog = builder.contextLog().stepInto(ClassScannerRecipe.class);
        final List<ResolvedMethod> publicMethods = fullType.publicMethods();

        for (final ResolvedMethod method : publicMethods) {
            if (!OBJECT_METHODS.contains(method.method().getName())) {
                final List<ResolvedType> offenders = new LinkedList<>();
                final List<? extends Definition> parameterDefinitions = method.parameters().stream()
                        .map(ResolvedParameter::type)
                        .collect(toList()).stream()
                        .map(DefinitionSeed::definitionSeed)
                        .map(seed -> seed.withCapability(deserializationOnly()))
                        .map(seed -> {
                            final Optional<? extends Definition> definition = detector.detect(seed, contextLog);
                            if (definition.isEmpty()) {
                                offenders.add(seed.type());
                            }
                            return definition;
                        })
                        .flatMap(Optional::stream)
                        .collect(toList());
                final Optional<? extends Definition> returnDefinition = method.returnType()
                        .map(DefinitionSeed::definitionSeed)
                        .map(seed -> seed.withCapability(serializationOnly()))
                        .flatMap(seed -> {
                            final Optional<? extends Definition> definition = detector.detect(seed, contextLog);
                            if (definition.isEmpty()) {
                                offenders.add(seed.type());
                            }
                            return definition;
                        });

                if (offenders.isEmpty()) {
                    returnDefinition.ifPresent(definition -> {
                        contextLog.log(definition.type(), "added because return type of method " + method.method().toString());
                        builder.withManuallyAddedDefinition(definition);
                    });
                    parameterDefinitions.forEach(definition -> {
                        contextLog.log(definition.type(), "added because parameter type of method " + method.method().toString());
                        builder.withManuallyAddedDefinition(definition);
                    });
                } else {
                    final String offendersString = offenders.stream()
                            .map(ResolvedType::description)
                            .collect(joining(", ", "[", "]"));
                    returnDefinition.ifPresent(definition -> contextLog.log(definition.type(), format(
                            "not added as return type of method %s because types not supported: %s",
                            method.method().toString(), offendersString)));

                    parameterDefinitions.forEach(definition -> contextLog.log(definition.type(), format(
                            "not added as parameter type of method %stypes not supported: %s",
                            method.method().toString(), offendersString)));
                }
            }
        }
    }
}
