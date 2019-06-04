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

package com.envimate.mapmate.serialization.methods;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.envimate.mapmate.serialization.SerializationException.fromException;
import static java.util.Arrays.stream;

public final class PublicFieldsSerializationDTOMethod implements SerializationDTOMethod {

    private PublicFieldsSerializationDTOMethod() {
    }

    public static SerializationDTOMethod thePublicFieldsSerializationDTOMethod() {
        return new PublicFieldsSerializationDTOMethod();
    }

    @Override
    public Object serialize(final Object object, final Function<Object, Object> serializerCallback) {
        final Field[] fields = object.getClass().getFields();
        final Map<String, Object> normalizedChildren = new HashMap<>(fields.length);

        stream(fields)
                .forEach(field -> {
                    try {
                        final String name = field.getName();
                        final Object value = field.get(object);
                        if (this.isNotTransient(field)) {
                            final Object serializedValue = serializerCallback.apply(value);
                            normalizedChildren.put(name, serializedValue);
                        } else {
                            normalizedChildren.put(name, null);
                        }

                    } catch (final IllegalAccessException e) {
                        throw fromException(e);
                    }
                });

        return normalizedChildren;
    }

    private boolean isNotTransient(final Field field) {
        return !Modifier.isTransient(field.getModifiers());
    }

}
