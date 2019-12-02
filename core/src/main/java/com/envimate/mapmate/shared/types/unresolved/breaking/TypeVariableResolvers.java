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

package com.envimate.mapmate.shared.types.unresolved.breaking;

import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeVariableResolvers {
    private final List<String> relevantTypeVariables;
    private final Map<String, Optional<TypeVariableResolver>> typeVariableResolvers;

    public static TypeVariableResolvers resolversFor(final Class<?> type) {
        final TypeVariable<? extends Class<?>>[] typeParameters = type.getTypeParameters();
        final List<String> names = stream(typeParameters)
                .map(TypeVariable::getName)
                .collect(toList());
        final Map<String, Optional<TypeVariableResolver>> resolvers = new HashMap<>(typeParameters.length);
        stream(typeParameters).forEach(typeVariable -> {
            final String name = typeVariable.getName();
            final Optional<TypeVariableResolver> typeVariableResolver = resolverForVariable(typeVariable, type);
            resolvers.put(name, typeVariableResolver);
        });
        return resolvers(names, resolvers);
    }

    private static Optional<TypeVariableResolver> resolverForVariable(final TypeVariable<?> typeVariable, final Class<?> type) {
        return stream(type.getFields())
                .filter(field -> typeVariable.equals(field.getGenericType()))
                .map(FieldTypeVariableResolver::fieldTypeVariableResolver)
                .findFirst();
    }

    private static TypeVariableResolvers resolvers(final List<String> relevantTypeVariables,
                                                   final Map<String, Optional<TypeVariableResolver>> resolverMap) {
        validateNotNull(relevantTypeVariables, "relevantTypeVariables");
        validateNotNull(resolverMap, "resolverMap");
        return new TypeVariableResolvers(relevantTypeVariables, resolverMap);
    }

    public List<ResolvedType> resolve(final Object object) {
        return this.relevantTypeVariables.stream()
                .map(this.typeVariableResolvers::get)
                .map(resolver -> resolver.orElseThrow(UnsupportedOperationException::new))
                .map(resolver -> resolver.resolve(object))
                .collect(toList());
    }
}
