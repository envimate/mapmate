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

package com.envimate.mapmate.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * CircularReferenceDetector provides ways to scan and detect circular references in a given object.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class CircularReferenceDetector {

    private static final int WRAPPER_COUNT = 10;
    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private static boolean isWrapperType(final Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        final Set<Class<?>> ret = new HashSet<>(WRAPPER_COUNT);
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        return ret;
    }

    /**
     * Detect scans a given object for circular references in its publicly accessible fields recursively.
     *
     * @param subject to be scanned
     * @throws CircularReferenceException if a circular reference is found.
     */
    void detect(final Object subject) {
        if (Objects.isNull(subject)) {
            return;
        }
        this.detect(subject, new ArrayList<>());
    }

    private void detect(final Object subject, final ArrayList<Object> references) {
        final Class<?> type = subject.getClass();
        final Field[] fields = type.getFields();
        for (final Field field : fields) {
            final Object value = this.readFieldValue(subject, field);
            if (value != null) {
                if (!isWrapperType(value.getClass())) {
                    if (references.contains(value)) {
                        final String message = String.format("a circular reference has been detected for objects of type %s",
                                value.getClass().getName());
                        throw new CircularReferenceException(message);
                    } else {
                        final ArrayList forkedReferences = (ArrayList) references.clone();
                        forkedReferences.add(value);
                        this.detect(value, forkedReferences);
                    }
                }
            }
        }
    }

    private Object readFieldValue(final Object subject, final Field field) {
        try {
            final Object value = field.get(subject);
            return value;
        } catch (final IllegalAccessException e) {
            throw new UnsupportedOperationException("could not read field value", e);
        }
    }
}
