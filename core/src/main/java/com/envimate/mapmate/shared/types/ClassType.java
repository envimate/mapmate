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

package com.envimate.mapmate.shared.types;

import com.envimate.mapmate.shared.types.resolver.ResolvedMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.shared.types.TypeVariableName.typeVariableNamesOf;
import static com.envimate.mapmate.shared.types.resolver.ResolvedMethod.resolvePublicMethods;
import static com.envimate.mapmate.shared.types.unresolved.UnresolvedType.unresolvedType;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassType implements ResolvedType {
    private final Class<?> clazz;
    private final Map<TypeVariableName, ResolvedType> typeParameters;

    public static ClassType typeOfObject(final Object object) {
        validateNotNull(object, "object");
        return unresolvedType(object.getClass()).resolveFromObject(object);
    }

    public static ClassType fromClassWithoutGenerics(final Class<?> type) {
        validateNotNull(type, "type");
        if (type.isArray()) {
            throw new UnsupportedOperationException();
        }
        if (type.getTypeParameters().length != 0) {
            throw new UnsupportedOperationException(format("Type variables of '%s' cannot be resolved", type.getName()));
        }
        return fromClassWithGenerics(type, emptyMap());
    }

    public static ClassType fromClassWithGenerics(final Class<?> type, final Map<TypeVariableName, ResolvedType> typeParameters) {
        validateNotNull(type, "type");
        validateNotNull(typeParameters, "typeParameters");
        if (type.isArray()) {
            throw new UnsupportedOperationException();
        }
        return new ClassType(type, typeParameters);
    }

    public ResolvedType typeParameter(final TypeVariableName name) {
        if (!this.typeParameters.containsKey(name)) {
            throw new IllegalArgumentException("No type parameter with the name: " + name.name());
        }
        return this.typeParameters.get(name);
    }

    @Override
    public List<ResolvedType> typeParameters() {
        return typeVariableNamesOf(this.clazz).stream()
                .map(this.typeParameters::get)
                .collect(toList());
    }

    public ResolvedType resolveTypeVariable(final TypeVariableName name) {
        if (!this.typeParameters.containsKey(name)) {
            throw new UnsupportedOperationException(format("No type variable with name '%s'", name.name()));
        }
        return this.typeParameters.get(name);
    }

    public List<ResolvedMethod> publicMethods() {
        return resolvePublicMethods(this);
    }

    @Override
    public String description() {
        if (this.typeParameters.isEmpty()) {
            return this.clazz.getName();
        }
        final String parametersString = this.typeParameters().stream()
                .map(ResolvedType::description)
                .collect(joining(", ", "<", ">"));
        return this.clazz.getName() + parametersString;
    }

    @Override
    public boolean isAbstract() {
        if (this.clazz.isPrimitive()) {
            return false;
        }
        return Modifier.isAbstract(this.clazz.getModifiers());
    }

    @Override
    public boolean isInterface() {
        return this.clazz.isInterface();
    }

    @Override
    public boolean isWildcard() {
        return false;
    }

    @Override
    public Class<?> assignableType() {
        return this.clazz;
    }
}
