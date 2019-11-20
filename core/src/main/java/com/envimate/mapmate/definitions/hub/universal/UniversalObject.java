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

package com.envimate.mapmate.definitions.hub.universal;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.envimate.mapmate.definitions.hub.universal.UniversalType.fromNativeJava;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniversalObject implements UniversalType {
    private final Map<String, UniversalType> map;

    public static UniversalObject universalObjectFromNativeMap(final Map<String, Object> map) {
        validateNotNull(map, "map");
        final Map<String, UniversalType> mappedMap = map.entrySet().stream()
                .collect(toMap(Entry::getKey, entry -> fromNativeJava(entry.getValue())));
        return universalObject(mappedMap);
    }

    public static UniversalObject universalObject(final Map<String, UniversalType> map) {
        validateNotNull(map, "map");
        map.forEach((key, value) -> validateNotNull(value, key));
        return new UniversalObject(map);
    }

    public Optional<UniversalType> getField(final String name) {
        validateNotNull(name, "name");
        if (!this.map.containsKey(name)) {
            return empty();
        }
        return of(this.map.get(name));
    }

    @Override
    public String nativeJavaTypeName() {
        return "Map<String, Object>";
    }

    @Override
    public Object toNativeJava() {
        final Map<String, Object> nativeMap = new HashMap<>(this.map.size());
        this.map.forEach((key, value) -> nativeMap.put(key, value.toNativeJava()));
        return nativeMap;
    }
}
