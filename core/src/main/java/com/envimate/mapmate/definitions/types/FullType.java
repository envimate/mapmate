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

package com.envimate.mapmate.definitions.types;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.*;
import java.util.List;

import static com.envimate.mapmate.definitions.types.UnsupportedJvmFeatureInTypeException.unsupportedJvmFeatureInTypeException;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.lang.reflect.Array.newInstance;
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
        return fullType(object.getClass());
    }

    public static FullType typeOfParameter(final Parameter parameter) {
        validateNotNull(parameter, "parameter");
        final Type type = parameter.getParameterizedType();
        return fromType(type);
    }

    public static FullType typeOfField(final Field field) {
        validateNotNull(field, "field");
        final Type type = field.getGenericType();
        return fromType(type);
    }

    public static FullType fromType(final Type type) {
        validateNotNull(type, "type");
        if (type instanceof Class) {
            return fullType((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return fromParameterizedType((ParameterizedType) type);
        }
        if (type instanceof GenericArrayType) {
            return fromGenericArrayType((GenericArrayType) type);
        }
        if(type instanceof WildcardType) {
            return fromWildcardType((WildcardType) type);
        }
        if(type instanceof TypeVariable) {
            return fromTypeVariable((TypeVariable<?>) type);
        }
        throw unsupportedJvmFeatureInTypeException(format("Unsupported 'Type' implementation by class '%s' on object '%s'", type.getClass(), type));
    }

    private static FullType fromParameterizedType(final ParameterizedType type) {
        final Type[] actualTypeArguments = type.getActualTypeArguments();
        final List<FullType> typeParameters = stream(actualTypeArguments)
                .map(FullType::fromType)
                .collect(toList());
        return parameterizedType((Class<?>) type.getRawType(), typeParameters);
    }

    private static FullType fromGenericArrayType(final GenericArrayType type) {
        final Type componentType = type.getGenericComponentType();
        final FullType fullComponentType = fromType(componentType);
        final Class<?> arrayType = newInstance(fullComponentType.type, 0).getClass();
        return parameterizedType(arrayType, singletonList(fullComponentType));
    }

    private static FullType fromWildcardType(final WildcardType type) {
        throw unsupportedJvmFeatureInTypeException(format("MapMate does not support wildcard generics but found '%s'", type));
    }

    private static FullType fromTypeVariable(final TypeVariable<?> type) {
        throw unsupportedJvmFeatureInTypeException(
                format("MapMate does not support type variables but found '%s'", type.getName()));
    }

    public static FullType fullType(final Class<?> type) {
        validateNotNull(type, "type");
        final List<FullType> typeParameters;
        if (type.isArray()) {
            final FullType componentType = fullType(type.getComponentType());
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
            throw new UnsupportedOperationException(format("Arrays need to have exactly one type parameter but found '%s' for array '%s'",
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
        if (this.type.isArray()) {
            final FullType theSingleTypeParameter = this.typeParameters.get(0);
            return theSingleTypeParameter.description() + "[]";
        }
        final String parametersString = this.typeParameters.stream()
                .map(FullType::description)
                .collect(joining(", ", "<", ">"));
        return this.type.getName() + parametersString;
    }
}
