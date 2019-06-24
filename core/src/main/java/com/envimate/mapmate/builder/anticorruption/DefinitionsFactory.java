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

package com.envimate.mapmate.builder.anticorruption;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDeserializer;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveSerializer;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.deserialization.*;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.serialization.*;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.builder.anticorruption.DeserializationCPMethodAdapter.deserializationCPMethodAdapter;
import static com.envimate.mapmate.builder.anticorruption.SerializationCPMethodAdapter.serializationCPMethodAdapter;
import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.deserialization.DeserializableDataTransferObject.deserializableDataTransferObject;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.deserializableDefinitions;
import static com.envimate.mapmate.serialization.SerializableCustomPrimitive.serializableCustomPrimitive;
import static com.envimate.mapmate.serialization.SerializableDataTransferObject.serializableDataTransferObject;
import static com.envimate.mapmate.serialization.SerializableDefinitions.serializableDefinitions;


@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionsFactory {
    private final Collection<CustomPrimitiveDefinition> customPrimitiveDefinitions;
    private final Collection<SerializedObjectDefinition> serializedObjectDefinitions;

    public static DefinitionsFactory definitionsFactory(final Collection<CustomPrimitiveDefinition> customPrimitives,
                                                        final Collection<SerializedObjectDefinition> serializedObjects) {
        return new DefinitionsFactory(customPrimitives, serializedObjects);
    }

    public SerializableDefinitions toSerializableDefinitions() {
        final List<SerializableCustomPrimitive> serializableCPs = new LinkedList<>();
        for (final CustomPrimitiveDefinition customPrimitiveDefinition : this.customPrimitiveDefinitions) {
            final Class<?> type = customPrimitiveDefinition.type;
            final CustomPrimitiveSerializer<?> serializer = customPrimitiveDefinition.serializer;
            final SerializationCPMethod serializationCPMethod = serializationCPMethodAdapter(serializer);
            final SerializableCustomPrimitive serializableCP = serializableCustomPrimitive(type, serializationCPMethod);
            serializableCPs.add(serializableCP);
        }

        final List<SerializableDataTransferObject> serializableDTOs = new LinkedList<>();
        for (final SerializedObjectDefinition serializedObjectDefinition : this.serializedObjectDefinitions) {
            final SerializationDTOMethod serializer = serializedObjectDefinition.serializer;
            if (serializer != null) {
                final Class<?> type = serializedObjectDefinition.type;
                final SerializableDataTransferObject serializableDTO = serializableDataTransferObject(type, serializer);
                serializableDTOs.add(serializableDTO);
            }
        }

        return serializableDefinitions(serializableCPs, serializableDTOs);

    }

    public DeserializableDefinitions toDeserializableDefinitions() {
        final List<DeserializableCustomPrimitive<?>> deserializableCPs = new LinkedList<>();
        for (final CustomPrimitiveDefinition customPrimitiveDefinition : this.customPrimitiveDefinitions) {
            final Class<?> type = customPrimitiveDefinition.type;
            final CustomPrimitiveDeserializer<?> deserializer = customPrimitiveDefinition.deserializer;
            final DeserializationCPMethodAdapter adapter = deserializationCPMethodAdapter(deserializer);
            final DeserializableCustomPrimitive<?> deserializableCP = deserializableCustomPrimitive(type, adapter);
            deserializableCPs.add(deserializableCP);
        }

        final List<DeserializableDataTransferObject<?>> deserializableDTOs = new LinkedList<>();
        for (final SerializedObjectDefinition serializedObjectDefinition : this.serializedObjectDefinitions) {
            final DeserializationDTOMethod deserializer = serializedObjectDefinition.deserializer;
            final Class<?> type = serializedObjectDefinition.type;

            if (deserializer != null) {
                final DeserializableDataTransferObject<?> deserializableDTO = deserializableDataTransferObject(
                        type,
                        deserializer
                );
                deserializableDTOs.add(deserializableDTO);
            }
        }

        return deserializableDefinitions(deserializableCPs, deserializableDTOs);
    }

}
