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

package com.envimate.mapmate.injector;

import com.envimate.mapmate.definitions.hub.FullType;
import com.envimate.mapmate.definitions.hub.universal.UniversalType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.definitions.hub.FullType.type;
import static com.envimate.mapmate.definitions.hub.FullType.typeOfObject;
import static com.envimate.mapmate.definitions.hub.universal.UniversalPrimitive.universalPrimitive;
import static com.envimate.mapmate.injector.NamedDirectInjection.namedDirectInjection;
import static com.envimate.mapmate.injector.PropertyName.propertyName;
import static com.envimate.mapmate.injector.TypedDirectInjection.typedDirectInjection;
import static com.envimate.mapmate.injector.UniversalInjection.universalInjection;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Injector {
    private final List<UniversalInjection> universalInjections = new LinkedList<>();
    private final List<NamedDirectInjection> namedDirectInjections = new LinkedList<>();
    private final List<TypedDirectInjection> typedDirectInjections = new LinkedList<>();

    static Injector empty() {
        return new Injector();
    }

    public Injector put(final String propertyName, final String value) {
        this.universalInjections.add(universalInjection(propertyName(propertyName), universalPrimitive(value)));
        return this;
    }

    public Injector put(final String propertyName, final Object instance) {
        this.namedDirectInjections.add(namedDirectInjection(propertyName(propertyName), instance));
        return this;
    }

    public Injector put(final Object instance) {
        return put(typeOfObject(instance), instance);
    }

    public Injector put(final Class<?> type, final Object instance) {
        return put(type(type), instance);
    }

    public Injector put(final FullType type, final Object instance) {
        this.typedDirectInjections.add(typedDirectInjection(type, instance));
        return this;
    }

    public Optional<UniversalType> getUniversalInjectionFor(final String position) {
        System.out.println("position = " + position);
        final PropertyName propertyName = propertyName(position);
        return this.universalInjections.stream()
                .filter(injection -> injection.propertyName().equals(propertyName))
                .findFirst()
                .map(UniversalInjection::value);
    }

    public Optional<Object> getDirectInjectionForPropertyPath(final String position) {
        final PropertyName propertyName = propertyName(position);
        return this.namedDirectInjections.stream()
                .filter(injection -> injection.propertyName().equals(propertyName))
                .findFirst()
                .map(NamedDirectInjection::value);
    }

    public Optional<Object> getDirectInjectionForType(final FullType type) {
        final Class<?> clazz = type.type();
        return this.typedDirectInjections.stream()
                .filter(injection -> clazz.isAssignableFrom(injection.type().type()))
                .findFirst()
                .map(TypedDirectInjection::value);
    }
}
