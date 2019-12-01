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

package com.envimate.mapmate.builder;

import com.envimate.mapmate.definitions.types.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.DefinitionSeed.definitionSeed;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionSeeds {
    private final List<DefinitionSeed> seeds;

    public static DefinitionSeeds definitionSeeds() {
        return new DefinitionSeeds(new LinkedList<>());
    }

    public void add(final SeedReason reason,
                    final FullType fullType,
                    final RequiredCapabilities requiredCapabilities) {
        final DefinitionSeed seed = forType(fullType);
        seed.addRequirements(reason, requiredCapabilities);
    }

    private DefinitionSeed forType(final FullType fullType) {
        return this.seeds.stream()
                .filter(definitionSeed -> definitionSeed.fullType().equals(fullType))
                .findAny()
                .orElseGet(() -> {
                    final DefinitionSeed seed = definitionSeed(fullType);
                    this.seeds.add(seed);
                    return seed;
                });
    }

    public List<DefinitionSeed> seeds() {
        return unmodifiableList(this.seeds);
    }

    public List<FullType> types() {
        return this.seeds.stream()
                .map(DefinitionSeed::fullType)
                .collect(Collectors.toList());
    }

    public RequiredCapabilities capabilitiesFor(final FullType type) {
        return forType(type).requiredCapabilities();
    }
}
