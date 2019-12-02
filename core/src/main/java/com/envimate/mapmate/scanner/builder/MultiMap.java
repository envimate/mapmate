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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiMap<K, V> {
    private final Map<K, List<V>> map;

    public static <K, V> MultiMap<K, V> multiMap(final int capacity) {
        return new MultiMap<>(new HashMap<>(capacity));
    }

    public List<V> get(final K key) {
        if (!this.map.containsKey(key)) {
            throw new IllegalArgumentException();
        }
        return unmodifiableList(this.map.get(key));
    }

    public void put(final K key, final V value) {
        this.map.computeIfAbsent(key, k -> new LinkedList<>()).add(value);
    }

    public void putMany(final K key, final Collection<V> values) {
        this.map.computeIfAbsent(key, k -> new LinkedList<>()).addAll(values);
    }

    public void putAll(final MultiMap<K, V> other) {
        other.map.forEach(this::putMany);
    }

    public Stream<V> flattenedValueStream() {
        return this.map.values().stream().flatMap(Collection::stream);
    }

    public Set<K> keys() {
        return this.map.keySet();
    }
}
