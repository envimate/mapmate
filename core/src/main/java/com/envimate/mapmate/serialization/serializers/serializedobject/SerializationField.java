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

package com.envimate.mapmate.serialization.serializers.serializedobject;

import com.envimate.mapmate.definitions.types.ResolvedType;
import com.envimate.mapmate.definitions.types.resolver.ResolvedField;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.function.Function;

import static com.envimate.mapmate.builder.detection.serializedobject.IncompatibleSerializedObjectException.incompatibleSerializedObjectException;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializationField {
    private final ResolvedType type;
    private final String name;
    private final Function<Object, Object> query;

    public static SerializationField serializationField(final ResolvedType type,
                                                        final String name,
                                                        final Function<Object, Object> query) {
        validateNotNull(type, "type");
        validateNotNull(name, "name");
        validateNotNull(query, "query");
        return new SerializationField(type, name, query);
    }

    public static SerializationField fromPublicField(final ResolvedType declaringType,
                                                     final ResolvedField field) {
        validateNotNull(declaringType, "declaringType");
        validateNotNull(field, "field");
        validateFieldModifiers(declaringType, field.field());
        final ResolvedType fullType = field.type();
        final String name = field.name();
        final Function<Object, Object> query = object -> readField(object, field.field());
        return serializationField(fullType, name, query);
    }

    public ResolvedType type() {
        return this.type;
    }

    public String name() {
        return this.name;
    }

    public Object query(final Object object) {
        validateNotNull(object, "object");
        return this.query.apply(object);
    }

    private static Object readField(final Object object, final Field field) {
        try {
            return field.get(object);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateFieldModifiers(final ResolvedType type, final Field field) {
        final int fieldModifiers = field.getModifiers();

        if (!isPublic(fieldModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The field %s for the SerializedObject of type %s must be public",
                    field, type.description());
        }
        if (isStatic(fieldModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The field %s for the SerializedObject of type %s must not be static",
                    field, type.description());
        }
        if (isTransient(fieldModifiers)) {
            throw incompatibleSerializedObjectException(
                    "The field %s for the SerializedObject of type %s must not be transient",
                    field, type.description());
        }
    }
}
