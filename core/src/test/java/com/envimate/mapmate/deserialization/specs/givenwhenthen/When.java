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

package com.envimate.mapmate.deserialization.specs.givenwhenthen;

import com.envimate.mapmate.deserialization.Deserializer;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class When {
    private final Deserializer deserializer;

    private static Then doDeserialization(final Supplier<Object> deserializer) {
        try {
            final Object result = deserializer.get();
            return new Then(result, null);
        } catch (final Exception e) {
            return new Then(null, e);
        }
    }

    public AsStage theDeserializerDeserializes(final String input) {
        return marshallingType -> type ->
                doDeserialization(() -> this.deserializer.deserialize(input, type, marshallingType));
    }

    @SuppressWarnings("unchecked")
    public ToStage theDeserializerDeserializesTheMap(final String jsonMap) {
        return type -> {
            final Map<String, Object> map = new Gson().fromJson(jsonMap, Map.class);
            return doDeserialization(() -> this.deserializer.deserializeFromMap(map, type));
        };
    }
}
