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

package com.envimate.mapmate.deserialization;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public final class DTOElements {

    private final Map<String, Class<?>> elements;

    private DTOElements(final Map<String, Class<?>> elements) {
        this.elements = elements;
    }

    static DTOElements theDTOElements(final Map<String, Class<?>> elements) {
        return new DTOElements(elements);
    }

    public Set<Class<?>> referencedTypes() {
        return this.elements.values()
                .stream()
                .map(type -> {
                    if (type.isArray()) {
                        return type.getComponentType();
                    }
                    return type;
                })
                .collect(toSet());
    }
}
