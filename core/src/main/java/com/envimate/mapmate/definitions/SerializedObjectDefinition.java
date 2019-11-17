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

package com.envimate.mapmate.definitions;

import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializedObjectSerializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializedObjectDefinition implements Definition {
    private final Class<?> type;
    private final SerializedObjectSerializer serializer;
    private final SerializedObjectDeserializer deserializer;

    public static SerializedObjectDefinition serializedObjectDefinition(final Class<?> type,
                                                                        final SerializedObjectSerializer serializer,
                                                                        final SerializedObjectDeserializer deserializer) {
        validateNotNull(type, "type");
        if (serializer == null) {
            validateNotNull(deserializer, "deserializer");
        } else if (deserializer == null) {
            validateNotNull(serializer, "serializer");
        }
        return new SerializedObjectDefinition(type, serializer, deserializer);
    }

    public SerializedObjectSerializer serializer() {
        return this.serializer;
    }

    public SerializedObjectDeserializer deserializer() {
        return this.deserializer;
    }

    @Override
    public boolean isCustomPrimitive() {
        return false;
    }

    @Override
    public boolean isSerializedObject() {
        return true;
    }

    @Override
    public Class<?> type() {
        return this.type;
    }
}
