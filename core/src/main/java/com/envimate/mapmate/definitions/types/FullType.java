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

import java.util.Map;

import static com.envimate.mapmate.definitions.types.TypeVariableName.arrayComponentName;
import static com.envimate.mapmate.definitions.types.unresolved.UnresolvedType.unresolvedType;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FullType {
    private final Class<?> type;
    private final Map<TypeVariableName, FullType> typeParameters;

    public static FullType typeOfObject(final Object object) {
        validateNotNull(object, "object");
        return unresolvedType(object.getClass()).resolveFromObject(object);
    }

    public static FullType fullType(final Class<?> type) {
        validateNotNull(type, "type");
        final Map<TypeVariableName, FullType> typeParameters;
        if (type.isArray()) {
            final FullType componentType = fullType(type.getComponentType());
            typeParameters = Map.of(arrayComponentName(), componentType);
        } else if(type.getTypeParameters().length == 0) {
            typeParameters = Map.of();
        } else {
            throw new UnsupportedOperationException(format("Type variables of '%s' cannot be resolved", type.getName()));
        }
        return parameterizedType(type, typeParameters);
    }

    public static FullType parameterizedType(final Class<?> type, final Map<TypeVariableName, FullType> typeParameters) {
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

    public Map<TypeVariableName, FullType> typeParameters() {
        return unmodifiableMap(this.typeParameters);
    }

    public FullType resolveTypeVariable(final TypeVariableName name) {
        if(!this.typeParameters.containsKey(name)) {
            throw new UnsupportedOperationException(format("No type variable with name '%s'", name.name()));
        }
        return this.typeParameters.get(name);
    }

    public String description() {
        if (this.typeParameters.isEmpty()) {
            return this.type.getName();
        }
        if (this.type.isArray()) {
            final FullType componentType = this.typeParameters.get(arrayComponentName());
            return componentType.description() + "[]";
        }
        final String parametersString = this.typeParameters().values().stream()
                .map(FullType::description)
                .collect(joining(", ", "<", ">"));
        return this.type.getName() + parametersString;
    }
}
