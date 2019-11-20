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

import java.util.List;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniversalCollection implements UniversalType {
    private final List<UniversalType> list;

    public static UniversalCollection universalCollectionFromNativeList(final List<Object> list) {
        validateNotNull(list, "list");
        final List<UniversalType> mappedList = list.stream()
                .map(UniversalType::fromNativeJava)
                .collect(toList());
        return universalCollection(mappedList);
    }

    public static UniversalCollection universalCollection(final List<UniversalType> list) {
        validateNotNull(list, "list");
        return new UniversalCollection(list);
    }

    public List<UniversalType> content() {
        return unmodifiableList(this.list);
    }

    @Override
    public String nativeJavaTypeName() {
        return "List";
    }

    @Override
    public Object toNativeJava() {
        return this.list.stream()
                .map(UniversalType::toNativeJava)
                .collect(toList());
    }
}
