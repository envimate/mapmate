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

package com.envimate.mapmate.builder.detection.serializedobject;

import com.envimate.mapmate.definitions.hub.FullType;

import java.util.List;
import java.util.regex.Pattern;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public interface ClassFilter {

    static ClassFilter allowAll() {
        return type -> true;
    }

    static ClassFilter patternFilter(final List<Pattern> patterns) {
        validateNotNull(patterns, "patterns");
        return type -> {
            final String typeName = type.type().getName();
            return patterns.stream().anyMatch(pattern -> pattern.matcher(typeName).matches());
        };
    }

    boolean filter(FullType type);
}
