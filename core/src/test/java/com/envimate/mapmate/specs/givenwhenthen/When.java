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

package com.envimate.mapmate.specs.givenwhenthen;

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.mapper.definitions.Definitions;
import com.envimate.mapmate.mapper.injector.InjectorLambda;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.mapmate.specs.givenwhenthen.Then.then;
import static com.envimate.mapmate.specs.givenwhenthen.ThenData.thenData;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When {
    private final MapMate mapMate;
    private final ThenData thenData;

    static When aWhen(final Supplier<MapMate> mapMateSupplier) {
        final ThenData thenData = thenData();
        MapMate mapMate;
        try {
            mapMate = mapMateSupplier.get();
        } catch (final Exception e) {
            thenData.withException(e);
            mapMate = null;
        }
        return new When(mapMate, thenData);
    }

    private Then doDeserialization(final Supplier<Object> deserializer) {
        try {
            final Object result = deserializer.get();
            return then(this.thenData.withDeserializationResult(result));
        } catch (final Exception e) {
            return then(this.thenData.withException(e));
        }
    }

    public AsStage mapMateDeserializes(final String input) {
        return marshallingType -> type ->
                doDeserialization(() -> this.mapMate.deserialize(input, type, marshallingType));
    }

    public AsStage mapMateDeserializesWithInjection(final String input, final InjectorLambda injector) {
        return marshallingType -> type ->
                doDeserialization(() -> this.mapMate.deserializer().deserialize(input, type, marshallingType, injector));
    }

    @SuppressWarnings("unchecked")
    public ToStage mapMateDeserializesTheMap(final String jsonMap) {
        return type -> {
            final Map<String, Object> map = new Gson().fromJson(jsonMap, Map.class);
            return doDeserialization(() -> this.mapMate.deserializer().deserializeFromMap(map, type));
        };
    }

    public WithMarshallingType mapMateSerializes(final Object object) {
        return marshallingType -> {
            try {
                final String serialized = this.mapMate.serializeTo(object, marshallingType);
                return then(this.thenData.withSerializationResult(serialized));
            } catch (final Exception e) {
                return then(this.thenData.withException(e));
            }
        };
    }

    public WithMarshallingType mapMateSerializesWithInjector(final Object object,
                                                             final Function<Map<String, Object>, Map<String, Object>> injector) {
        return marshallingType -> {
            try {
                final String serialized = this.mapMate.serializer().serialize(object, marshallingType, injector);
                return then(this.thenData.withSerializationResult(serialized));
            } catch (final Exception e) {
                return then(this.thenData.withException(e));
            }
        };
    }

    public Then theDefinitionsAreQueried() {
        final Definitions definitions = this.mapMate.deserializer().getDefinitions();
        return then(this.thenData.withDefinitions(definitions));
    }

    public Then mapMateIsInstantiated() {
        return then(this.thenData);
    }
}
