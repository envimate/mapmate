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

package com.envimate.mapmate.definitions.types.unresolved;

import com.envimate.mapmate.definitions.types.ClassType;
import com.envimate.mapmate.definitions.types.ResolvedType;
import com.envimate.mapmate.definitions.types.TypeVariableName;
import com.envimate.mapmate.definitions.types.unresolved.breaking.TypeVariableResolvers;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.definitions.types.ClassType.fromClassWithGenerics;
import static com.envimate.mapmate.definitions.types.TypeVariableName.typeVariableNamesOf;
import static com.envimate.mapmate.definitions.types.unresolved.breaking.TypeVariableResolvers.resolversFor;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnresolvedType {
    private final Class<?> type;
    private final List<TypeVariableName> variables;
    private final TypeVariableResolvers resolvers;

    public static UnresolvedType unresolvedType(final Class<?> type) {
        validateNotNull(type, "type");
        final TypeVariableResolvers resolvers = resolversFor(type);
        return new UnresolvedType(type, typeVariableNamesOf(type), resolvers);
    }

    public ClassType resolve(final ResolvedType... values) {
        if (values.length != this.variables.size()) {
            throw new IllegalArgumentException();
        }
        final Map<TypeVariableName, ResolvedType> resolvedParameters = new HashMap<>(values.length);
        for (int i = 0; i < this.variables.size(); ++i) {
            final TypeVariableName name = this.variables.get(i);
            final ResolvedType value = values[i];
            resolvedParameters.put(name, value);
        }
        return fromClassWithGenerics(this.type, resolvedParameters);
    }

    public ClassType resolveFromObject(final Object object) {
        final List<ResolvedType> typeList = this.resolvers.resolve(object);
        return resolve(typeList.toArray(ResolvedType[]::new));
    }
}
