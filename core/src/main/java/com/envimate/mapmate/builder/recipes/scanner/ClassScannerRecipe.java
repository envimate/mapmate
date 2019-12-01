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
import com.envimate.mapmate.builder.SeedReason;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.definitions.types.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.RequiredCapabilities.deserializationOnly;
import static com.envimate.mapmate.builder.RequiredCapabilities.serializationOnly;
import static com.envimate.mapmate.builder.SeedReason.becauseParameterTypeOfUseCaseMethod;
import static com.envimate.mapmate.builder.SeedReason.becauseReturnTypeOfUseCaseMethod;
import static com.envimate.mapmate.definitions.types.resolver.TypeResolver.resolveType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
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
    public void cook(final MapMateBuilder mapMateBuilder) {
        this.classes.forEach(clazz -> addReferencesIn(clazz, mapMateBuilder));
    }

    private static void addReferencesIn(final Class<?> clazz, final MapMateBuilder builder) {
        final FullType fullType = FullType.fullType(clazz);

        final List<Method> useCaseMethodCandidates = stream(clazz.getDeclaredMethods())
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> !OBJECT_METHODS.contains(method.getName()))
                .collect(toList());

        useCaseMethodCandidates.forEach(method -> {
            if (method.getReturnType() != Void.TYPE) {
                resolveType(method.getGenericReturnType(), fullType)
                        .ifPresent(returnType -> builder.withManuallyAddedType(
                                becauseReturnTypeOfUseCaseMethod(method), returnType, serializationOnly())
                        );
            }
            stream(method.getParameters())
                    .map(Parameter::getParameterizedType)
                    .map(type -> resolveType(type, fullType))
                    .flatMap(Optional::stream)
                    .forEach(parameterType -> builder.withManuallyAddedType(
                            becauseParameterTypeOfUseCaseMethod(method),parameterType, deserializationOnly())
                    );
        });
    }
}
