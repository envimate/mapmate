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
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.definitions.types.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.definitions.types.FullType.fromType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassScannerRecipe implements Recipe {
    private final List<Class<?>> classes;

    public static ClassScannerRecipe addAllReferencesClassesIs(final Class<?>... classes) {
        validateNotNull(classes, "classes");
        return new ClassScannerRecipe(asList(classes));
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder) {
        this.classes.stream()
                .map(ClassScannerRecipe::referencesIn)
                .flatMap(Collection::stream)
                .forEach(mapMateBuilder::withManuallyAddedType);
    }

    private static List<FullType> referencesIn(final Class<?> clazz) {
        final List<FullType> classes = new LinkedList<>();
        stream(clazz.getFields())
                .filter(field -> isPublic(field.getModifiers()))
                .map(FullType::typeOfField)
                .forEach(classes::add);
        stream(clazz.getDeclaredMethods())
                .filter(field -> isPublic(field.getModifiers()))
                .forEach(method -> {
                    classes.add(fromType(method.getGenericReturnType()));
                    stream(method.getParameters())
                            .map(FullType::typeOfParameter)
                            .forEach(classes::add);
                });
        stream(clazz.getConstructors())
                .filter(field -> isPublic(field.getModifiers()))
                .map(Executable::getParameters)
                .flatMap(Arrays::stream)
                .map(FullType::typeOfParameter)
                .forEach(classes::add);
        return classes;
    }
}
