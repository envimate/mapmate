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

package com.envimate.mapmate.definitions.hub;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FullType {
    private final Class<?> type;
    private final List<FullType> typeParameters;

    public static FullType typeOfObject(final Object object) {
        validateNotNull(object, "object");
        return type(object.getClass());
    }

    public static FullType typeOfParameter(final Parameter parameter) {
        validateNotNull(parameter, "parameter");
        final Type type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType) {
            return parameterizedType(parameter.getType(), fromParameterizedType((ParameterizedType) type));
        } else {
            return type(parameter.getType());
        }
    }

    public static FullType typeOfField(final Field field) {
        validateNotNull(field, "field");
        final Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            return parameterizedType(field.getType(), fromParameterizedType((ParameterizedType) type));
        } else {
            return type(field.getType());
        }
    }

    private static List<FullType> fromParameterizedType(final ParameterizedType type) {
        final Type[] actualTypeArguments = type.getActualTypeArguments();
        return stream(actualTypeArguments)
                .map(clazz -> type((Class<?>) clazz))
                .collect(toList());
    }

    public static FullType type(final Class<?> type) {
        validateNotNull(type, "type");
        final List<FullType> typeParameters;
        if (type.isArray()) {
            final FullType componentType = type(type.getComponentType());
            typeParameters = singletonList(componentType);
        } else {
            typeParameters = emptyList();
        }
        return parameterizedType(type, typeParameters);
    }

    public static FullType parameterizedType(final Class<?> type, final List<FullType> typeParameters) {
        validateNotNull(type, "type");
        validateNotNull(typeParameters, "typeParameters");
        if (type.isArray() && typeParameters.size() != 1) {
            throw new UnsupportedOperationException(format(
                    "Arrays need to have exactly one type parameter but found '%s' for array '%s'",
                    typeParameters, type));
        }
        return new FullType(type, typeParameters);
    }

    public Class<?> type() {
        return this.type;
    }

    public List<FullType> typeParameters() {
        return unmodifiableList(this.typeParameters);
    }

    public String description() {
        if (this.typeParameters.isEmpty()) {
            return this.type.getName();
        }
        if(this.type.isArray()) {
            final FullType theSingleTypeParameter = this.typeParameters.get(0);
            return theSingleTypeParameter.description() + "[]";
        }
        final String parametersString = this.typeParameters.stream()
                .map(FullType::description)
                .collect(joining(", ", "<", ">"));
        return this.type.getName() + parametersString;
    }
}
