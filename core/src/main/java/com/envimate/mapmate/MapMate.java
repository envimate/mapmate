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

package com.envimate.mapmate;

import com.envimate.mapmate.mapper.deserialization.Deserializer;
import com.envimate.mapmate.mapper.marshalling.MarshallingType;
import com.envimate.mapmate.mapper.serialization.Serializer;
import com.envimate.mapmate.builder.MapMateBuilder;
import com.envimate.mapmate.builder.scanning.PackageScanner;
import com.envimate.mapmate.shared.types.ClassType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMate {
    private final Serializer serializer;
    private final Deserializer deserializer;

    public static MapMateBuilder aMapMate(final String... packageNames) {
        return MapMateBuilder.mapMateBuilder(packageNames);
    }

    public static MapMateBuilder aMapMate(final PackageScanner packageScanner) {
        return MapMateBuilder.mapMateBuilder(packageScanner);
    }

    public static MapMate mapMate(final Serializer serializer, final Deserializer deserializer) {
        return new MapMate(serializer, deserializer);
    }

    public Serializer serializer() {
        return this.serializer;
    }

    public Deserializer deserializer() {
        return this.deserializer;
    }

    public String serializeToJson(final Object object) {
        return this.serializer.serializeToJson(object);
    }

    public String serializeToYaml(final Object object) {
        return this.serializer.serialize(object, MarshallingType.yaml());
    }

    public String serializeToXml(final Object object) {
        return this.serializer.serialize(object, MarshallingType.xml());
    }

    public String serializeTo(final Object object, final MarshallingType marshallingType) {
        return this.serializer.serialize(object, marshallingType);
    }

    public <T> T deserializeJson(final String json, final Class<T> targetType) {
        return this.deserializer.deserializeJson(json, targetType);
    }

    public <T> T deserializeYaml(final String yaml, final Class<T> targetType) {
        return this.deserializer.deserialize(yaml, targetType, MarshallingType.yaml());
    }

    public <T> T deserializeXml(final String xml, final Class<T> targetType) {
        return this.deserializer.deserialize(xml, targetType, MarshallingType.xml());
    }

    public <T> T deserialize(final String json, final Class<T> targetType, final MarshallingType marshallingType) {
        return this.deserializer.deserialize(json, targetType, marshallingType);
    }

    public <T> T deserialize(final String json, final ClassType targetType, final MarshallingType marshallingType) {
        return this.deserializer.deserialize(json, targetType, marshallingType);
    }
}
