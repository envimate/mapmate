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

package com.envimate.mapmate.definitions.types.resolver;

import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.definitions.types.TypeVariableName;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.envimate.mapmate.definitions.types.FullType.fullType;
import static com.envimate.mapmate.definitions.types.FullType.parameterizedType;
import static com.envimate.mapmate.definitions.types.TypeVariableName.*;
import static com.envimate.mapmate.definitions.types.UnsupportedJvmFeatureInTypeException.unsupportedJvmFeatureInTypeException;
import static java.lang.String.format;
import static java.lang.reflect.Array.newInstance;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class TypeResolver {

    private TypeResolver() {
    }

    public static Optional<FullType> resolveType(final Type type, final FullType fullType) {
        if (type instanceof Class) {
            return of(fullType((Class<?>) type));
        }
        if (type instanceof TypeVariable) {
            return of(resolveTypeVariable((TypeVariable<?>) type, fullType));
        }
        if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, fullType);
        }
        if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, fullType);
        }
        if(type instanceof WildcardType) {
            return empty();
        }
        throw unsupportedJvmFeatureInTypeException(format("Unknown 'Type' implementation by class '%s' on object '%s'", type.getClass(), type));
    }

    private static FullType resolveTypeVariable(final TypeVariable<?> typeVariable, final FullType fullType) {
        final TypeVariableName typeVariableName = typeVariableName(typeVariable);
        return fullType.resolveTypeVariable(typeVariableName);
    }

    private static Optional<FullType> resolveParameterizedType(final ParameterizedType parameterizedType, final FullType fullType) {
        final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        final List<TypeVariableName> typeVariableNames = typeVariableNamesOf(rawType);

        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        final Map<TypeVariableName, FullType> typeParameters = new HashMap<>(actualTypeArguments.length);
        for (int i = 0; i < actualTypeArguments.length; ++i) {
            final Optional<FullType> resolvedTypeArgument = resolveType(actualTypeArguments[i], fullType);
            if (resolvedTypeArgument.isEmpty()) {
                return empty();
            }
            final TypeVariableName name = typeVariableNames.get(i);
            typeParameters.put(name, resolvedTypeArgument.get());
        }

        return of(parameterizedType(rawType, typeParameters));
    }

    private static Optional<FullType> resolveGenericArrayType(final GenericArrayType genericArrayType, final FullType fullType) {
        final Type componentType = genericArrayType.getGenericComponentType();
        return resolveType(componentType, fullType).map(fullComponentType -> {
            final Class<?> arrayType = newInstance(fullComponentType.type(), 0).getClass();
            return parameterizedType(arrayType, Map.of(arrayComponentName(), fullComponentType));
        });
    }
}
