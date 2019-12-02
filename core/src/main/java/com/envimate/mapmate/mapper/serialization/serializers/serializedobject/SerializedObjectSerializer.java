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

package com.envimate.mapmate.mapper.serialization.serializers.serializedobject;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.definitions.universal.UniversalPrimitive;
import com.envimate.mapmate.mapper.serialization.SerializationCallback;
import com.envimate.mapmate.mapper.serialization.serializers.TypeSerializer;
import com.envimate.mapmate.mapper.serialization.tracker.SerializationTracker;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.envimate.mapmate.mapper.definitions.universal.UniversalObject.universalObject;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.util.Optional.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectSerializer implements TypeSerializer {
    private final SerializationFields fields;

    public static Optional<SerializedObjectSerializer> serializedObjectSerializer(final SerializationFields fields) {
        validateNotNull(fields, "fields");
        if (fields.isEmpty()) {
            return empty();
        }
        return of(new SerializedObjectSerializer(fields));
    }

    public SerializationFields fields() {
        return this.fields;
    }

    @Override
    public List<ResolvedType> requiredTypes() {
        return this.fields.typesList();
    }

    @Override
    public Universal serialize(final Definition definition,
                               final Object object,
                               final SerializationCallback callback,
                               final SerializationTracker tracker,
                               final CustomPrimitiveMappings customPrimitiveMappings) {
        final SerializationFields fields = fields();
        final Map<String, Universal> map = new HashMap<>(10);
        fields.fields().forEach(serializationField -> {
            final ResolvedType type = serializationField.type();
            final Object value = ofNullable(object).map(serializationField::query).orElse(null);
            final Universal serializedValue = callback.serializeDefinition(type, value, tracker);
            final String name = serializationField.name();
            map.put(name, serializedValue);
        });
        return universalObject(map);
    }
}
