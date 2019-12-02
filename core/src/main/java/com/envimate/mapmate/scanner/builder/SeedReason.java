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

package com.envimate.mapmate.scanner.builder;

import com.envimate.mapmate.mapper.definitions.Definition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeedReason {
    private final List<SeedReason> causes = new LinkedList<>();
    private final String description;

    public static SeedReason combinedSeedReason(final Set<SeedReason> reasons) {
        final SeedReason reason = new SeedReason("Combination of other reasons");
        reason.causes.addAll(reasons);
        return reason;
    }

    public static SeedReason becauseChildOf(final DefinitionSeed parent) {
        return new SeedReason("Serialization Child of " + parent);
    }

    public static SeedReason becauseDeserializationParameterOf(final Definition definition) {
        return new SeedReason("Deserialization Parameter of " + definition);
    }

    public static SeedReason manuallyAdded() {
        return new SeedReason("Manually Added");
    }

    public static SeedReason becauseReturnTypeOfUseCaseMethod(final Method method) {
        return new SeedReason("Return type of use case method " + method);
    }

    public static SeedReason becauseParameterTypeOfUseCaseMethod(final Method method) {
        return new SeedReason("Return parameter of use case method " + method);
    }

    public static SeedReason manuallyAddedIn(final Class<?> aClass, final String method) {
        return new SeedReason("Manually added in method " + method + " of class " + aClass);
    }
}
