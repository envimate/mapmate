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

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.mapper.deserialization.DeserializationFields;
import com.envimate.mapmate.mapper.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializedObjectSerializer;
import com.envimate.mapmate.shared.types.ClassType;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializationField.serializationField;
import static com.envimate.mapmate.shared.types.ClassType.fromClassWithoutGenerics;

public class Main {

    public static void main(String[] args) {

    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class CustomDefinition {
        private final List<ResolvedType> possiblePropertyTypes;
        private final ResolvedType myType;
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class MyCustomer {
        private final String name;
        private final MyAddress address;

        public static Definition definition() {
            final ClassType stringType = fromClassWithoutGenerics(String.class);
            final ClassType addressType = fromClassWithoutGenerics(MyAddress.class);
            final SerializedObjectSerializer serializer = SerializedObjectSerializer.serializedObjectSerializer(SerializationFields.serializationFields(List.of(
                    serializationField(stringType, "name", object -> ((MyCustomer) object).name),
                    serializationField(addressType, "address", object -> ((MyCustomer) object).address)
            ))).orElseThrow();

            final SerializedObjectDeserializer deserializer = new SerializedObjectDeserializer() {

                @Override
                public Object deserialize(final Map<String, Object> elements) throws Exception {
                    return new MyCustomer((String) elements.get("name"), (MyAddress) elements.get("address"));
                }

                @Override
                public DeserializationFields fields() {
                    return DeserializationFields.deserializationFields(Map.of("name", stringType, "address", addressType));
                }
            };

            return SerializedObjectDefinition.serializedObjectDefinition(fromClassWithoutGenerics(MyCustomer.class), serializer, deserializer);
        }
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class MyEmployee {
        private final String name;
        private final int age;
        private final MyAddress address;
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class MyAddress {
        private final String street;
        private final String zip;
        private final String town;

        public static Definition definition() {
            final ClassType stringType = fromClassWithoutGenerics(String.class);
            final SerializedObjectSerializer serializer = SerializedObjectSerializer.serializedObjectSerializer(SerializationFields.serializationFields(List.of(
                    serializationField(stringType, "street", object -> ((MyAddress) object).street),
                    serializationField(stringType, "zip", object -> ((MyAddress) object).zip),
                    serializationField(stringType, "town", object -> ((MyAddress) object).town))
            )).orElseThrow();

            final SerializedObjectDeserializer deserializer = new SerializedObjectDeserializer() {

                @Override
                public Object deserialize(final Map<String, Object> elements) throws Exception {
                    return new MyAddress((String) elements.get("street"), (String) elements.get("zip"), (String) elements.get("town"));
                }

                @Override
                public DeserializationFields fields() {
                    return DeserializationFields.deserializationFields(Map.of("street", stringType, "zip", stringType, "town", stringType));
                }
            };

            return SerializedObjectDefinition.serializedObjectDefinition(fromClassWithoutGenerics(MyAddress.class), serializer, deserializer);
        }
    }
}

