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

import java.lang.reflect.Type;
import java.util.List;

public interface ResolvedType {

    static ResolvedType resolveType(final Type type, final ClassType fullType) {
        return TypeResolver.resolveType(type, fullType);
    }

    Class<?> assignableType();

    List<ResolvedType> typeParameters();

    boolean isAbstract();

    boolean isInterface();

    boolean isWildcard();

    String description();

    default boolean isInstantiatable() {
        if (isAbstract()) {
            return false;
        }
        if (isInterface()) {
            return false;
        }
        if (isWildcard()) {
            return false;
        }
        return typeParameters().stream()
                .allMatch(ResolvedType::isInstantiatable);
    }

    // START EXTERNAL
    public static boolean isSupported(final ResolvedType type) {
        if (type.isAbstract()) {
            return false;
        }
        if (type.isInterface()) {
            return false;
        }
        if (type.isWildcard()) {
            return false;
        }
        return type.typeParameters().stream()
                .anyMatch(type1 -> !isSupported(type1));
    }
}
