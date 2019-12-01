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

import com.envimate.mapmate.definitions.ArrayType;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.definitions.ArrayType.arrayType;
import static com.envimate.mapmate.definitions.types.ClassType.fromClassWithGenerics;
import static com.envimate.mapmate.definitions.types.ClassType.fromClassWithoutGenerics;
import static com.envimate.mapmate.definitions.types.TypeVariableName.typeVariableName;
import static com.envimate.mapmate.definitions.types.TypeVariableName.typeVariableNamesOf;
import static com.envimate.mapmate.definitions.types.UnsupportedJvmFeatureInTypeException.unsupportedJvmFeatureInTypeException;
import static com.envimate.mapmate.definitions.types.WildcardedType.wildcardType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;

public final class TypeResolver {

    private TypeResolver() {
    }

    public static ResolvedType resolveType(final Type type, final ClassType context) {
        if (type instanceof Class) {
            return resolveClass((Class<?>) type, context);
        }
        if (type instanceof TypeVariable) {
            return resolveTypeVariable((TypeVariable<?>) type, context);
        }
        if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, context);
        }
        if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, context);
        }
        if (type instanceof WildcardType) {
            return wildcardType();
        }
        throw unsupportedJvmFeatureInTypeException(format("Unknown 'Type' implementation by class '%s' on object '%s'", type.getClass(), type));
    }

    private static ResolvedType resolveClass(final Class<?> clazz, final ClassType fullType) {
        validateNotNull(clazz, "clazz");
        if (clazz.isArray()) {
            final ResolvedType componentType = resolveType(clazz.getComponentType(), fullType);
            return arrayType(componentType);
        } else {
            return fromClassWithoutGenerics(clazz);
        }
    }

    private static ResolvedType resolveTypeVariable(final TypeVariable<?> typeVariable, final ClassType fullType) {
        final TypeVariableName typeVariableName = typeVariableName(typeVariable);
        return fullType.resolveTypeVariable(typeVariableName);
    }

    private static ResolvedType resolveParameterizedType(final ParameterizedType parameterizedType, final ClassType context) {
        final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        final List<TypeVariableName> typeVariableNames = typeVariableNamesOf(rawType);

        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        final Map<TypeVariableName, ResolvedType> typeParameters = new HashMap<>(actualTypeArguments.length);
        for (int i = 0; i < actualTypeArguments.length; ++i) {
            final ResolvedType resolvedTypeArgument = resolveType(actualTypeArguments[i], context);
            final TypeVariableName name = typeVariableNames.get(i);
            typeParameters.put(name, resolvedTypeArgument);
        }

        return fromClassWithGenerics(rawType, typeParameters);
    }

    private static ArrayType resolveGenericArrayType(final GenericArrayType genericArrayType, final ClassType context) {
        final Type componentType = genericArrayType.getGenericComponentType();
        final ResolvedType fullComponentType = resolveType(componentType, context);
        return arrayType(fullComponentType);
    }
}
