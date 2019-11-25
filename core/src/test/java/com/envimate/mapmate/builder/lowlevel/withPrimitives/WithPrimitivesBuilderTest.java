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

package com.envimate.mapmate.builder.lowlevel.withPrimitives;

import com.envimate.mapmate.MapMate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.builder.recipes.marshallers.jackson.JacksonMarshaller.jacksonMarshallerJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class WithPrimitivesBuilderTest {
    private static final String JSON_WITH_STRING_SERIALIZED_PRIMITIVES = "{" +
            "\"floatPrimitive\":\"23.0\"," +
            "\"integerObject\":\"24\"," +
            "\"intPrimitive\":\"23\"," +
            "\"shortObject\":\"24\"," +
            "\"floatObject\":\"24.0\"," +
            "\"longObject\":\"24\"," +
            "\"stringObject\":\"23\"," +
            "\"booleanPrimitive\":\"true\"," +
            "\"longPrimitive\":\"23\"," +
            "\"doublePrimitive\":\"23.0\"," +
            "\"doubleObject\":\"24.0\"," +
            "\"shortPrimitive\":\"23\"," +
            "\"booleanObject\":\"false\"" +
            "}";

    private static final String JSON_WITH_SERIALIZED_PRIMITIVES = "{" +
            "\"floatPrimitive\":23.0," +
            "\"integerObject\":24," +
            "\"intPrimitive\":23," +
            "\"shortObject\":24," +
            "\"floatObject\":24.0," +
            "\"longObject\":24," +
            "\"stringObject\":23," +
            "\"booleanPrimitive\":true," +
            "\"longPrimitive\":23," +
            "\"doublePrimitive\":23.0," +
            "\"doubleObject\":24.0," +
            "\"shortPrimitive\":23," +
            "\"booleanObject\":false" +
            "}";

    private static final SerializedObjectWithPrimitives SERIALIZED_OBJECT = SerializedObjectWithPrimitives.deserialize(
            23,
            Integer.valueOf(24),
            23l,
            Long.valueOf(24),
            (short) 23,
            Short.valueOf((short) 24),
            23d,
            Double.valueOf(24d),
            23f,
            Float.valueOf(24f),
            true,
            Boolean.FALSE,
            "23"
    );

    private static final MapMate MAP_MATE = MapMate.aMapMate()
            .withManuallyAddedType(SerializedObjectWithPrimitives.class)
            .usingRecipe(jacksonMarshallerJson(new ObjectMapper()))
            .build();

    @Test
    public void testSerialization() {
        final String result = MAP_MATE
                .serializer()
                .serializeToJson(SERIALIZED_OBJECT);
        assertThat(result, is(JSON_WITH_STRING_SERIALIZED_PRIMITIVES));
    }

    @Test
    public void testDeserializationOfStringSerializedPrimitives() {
        final SerializedObjectWithPrimitives result = MAP_MATE
                .deserializer()
                .deserializeJson(JSON_WITH_STRING_SERIALIZED_PRIMITIVES, SerializedObjectWithPrimitives.class);
        assertThat(result, is(SERIALIZED_OBJECT));
    }

    @Test
    public void testDeserializationOfPrimitives() {
        final SerializedObjectWithPrimitives result = MAP_MATE
                .deserializer()
                .deserializeJson(JSON_WITH_SERIALIZED_PRIMITIVES, SerializedObjectWithPrimitives.class);
        assertThat(result, is(SERIALIZED_OBJECT));
    }
}
