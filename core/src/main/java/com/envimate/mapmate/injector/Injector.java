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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class Injector {

    private final Collection<Injection> injections;

    private Injector() {
        this.injections = new ArrayList<>(0);
    }

    public Injector put(final String propertyName, final String value) {
        this.injections.add(Injection.fromPropertyNameAndValue(propertyName, value));
        return this;
    }

    public Injector put(final String propertyName, final Object instance) {
        this.injections.add(Injection.fromPropertyNameAndInstance(propertyName, instance, instance.getClass()));
        return this;
    }

    public Injector put(final String propertyName, final Class<?> type, final Object instance) {
        this.injections.add(Injection.fromPropertyNameAndInstance(propertyName, instance, type));
        return this;
    }

    public Injector put(final Object instance) {
        this.injections.add(Injection.fromInstance(instance, instance.getClass()));
        return this;
    }

    public Injector put(final Class<?> type, final Object instance) {
        this.injections.add(Injection.fromInstance(instance, type));
        return this;
    }

    public static Injector empty() {
        return new Injector();
    }

    public Optional<Object> getInjectionForPropertyPath(final String position, final Class<?> targetType) {
        return this.injections.stream()
                .filter(Injection::containsPropertyName)
                .filter(injection -> injection.propertyName.equals(position))
                .map(Injection::getValue)
                .findFirst();
    }

    public Object getInjectionForPropertyNameOrInstance(final String propertyName, final Class<?> elementType) {
        final Object injected = this.injections.stream()
                .filter(Injection::containsPropertyName)
                .filter(injection -> injection.propertyName.equals(propertyName))
                .filter(injection -> injection.instanceValue != null)
                .filter(injection -> injection.type == elementType)
                .map(Injection::getValue)
                .findFirst().orElse(null);

        if (injected == null) {
            return this.injections.stream()
                    .filter(injection -> !injection.containsPropertyName())
                    .filter(injection -> injection.instanceValue != null)
                    .filter(injection -> injection.type == elementType)
                    .map(Injection::getValue)
                    .findFirst().orElse(null);
        } else {
            return injected;
        }
    }

    private static final class Injection {

        final String propertyName;
        final String stringValue;
        final Object instanceValue;
        final Class<?> type;
        private final boolean recursive;

        private Injection(final String propertyName, final String stringValue, final Object instanceValue, final Class<?> type) {
            this.propertyName = propertyName;
            this.stringValue = stringValue;
            this.instanceValue = instanceValue;
            this.type = type;
            this.recursive = false;
        }

        boolean containsPropertyName() {
            return this.propertyName != null;
        }

        Object getValue() {
            if (this.stringValue != null) {
                return this.stringValue;
            } else {
                return this.instanceValue;
            }
        }

        public boolean isRecursive() {
            return this.recursive;
        }

        static Injection fromPropertyNameAndValue(final String propertyName, final String value) {
            return new Injection(propertyName, value, null, null);
        }

        static Injection fromPropertyNameAndInstance(final String propertyName, final Object instance, final Class<?> type) {
            return new Injection(propertyName, null, instance, type);
        }

        static Injection fromInstance(final Object instance, final Class<?> type) {
            return new Injection(null, null, instance, type);
        }
    }
}
