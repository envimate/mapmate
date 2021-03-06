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

package com.envimate.mapmate.deserialization.specs;

import com.envimate.mapmate.domain.valid.AComplexType;
import org.junit.Test;

import static com.envimate.mapmate.deserialization.specs.givenwhenthen.Given.givenTheExampleMapMateDeserializer;
import static com.envimate.mapmate.marshalling.MarshallingType.json;

public final class ExceptionTrackingSpecs {

    @Test
    public void testUnmappedException() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "{\n" +
                "  \"number1\": \"x\",\n" +
                "  \"number2\": \"5\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").as(json()).toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("Unrecognized exception deserializing field 'number1': " +
                        "Exception calling deserialize(input: x) on definition DeserializableCustomPrimitive(type=class " +
                        "com.envimate.mapmate.domain.valid.ANumber, deserializationMethod=NamedFactoryMethodCPMethod(" +
                        "methodName=MethodName(value=fromString)))");
    }

    @Test
    public void testMappedException() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "{\n" +
                "  \"number1\": \"4\",\n" +
                "  \"number2\": \"5000\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").as(json()).toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("deserialization encountered validation errors." +
                        " Validation error at 'number2', value cannot be over 50; ");
    }

    @Test
    public void testUnmappedExceptionWithoutMarshalling() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializesTheMap("" +
                "{\n" +
                "  \"number1\": \"x\",\n" +
                "  \"number2\": \"5\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("Unrecognized exception deserializing field 'number1': " +
                        "Exception calling deserialize(input: x) on definition " +
                        "DeserializableCustomPrimitive(type=class com.envimate.mapmate.domain.valid.ANumber, " +
                        "deserializationMethod=NamedFactoryMethodCPMethod(methodName=MethodName(value=fromString)))");
    }

    @Test
    public void testMappedExceptionWithoutMarshalling() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializesTheMap("" +
                "{\n" +
                "  \"number1\": \"4\",\n" +
                "  \"number2\": \"5000\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("deserialization encountered validation errors." +
                        " Validation error at 'number2', value cannot be over 50; ");
    }

    @Test
    public void testMultipleExceptionsCanBeMapped() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "{\n" +
                "  \"number1\": \"4000\",\n" +
                "  \"number2\": \"5000\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").as(json()).toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("deserialization encountered validation errors." +
                        " Validation error at 'number1', value cannot be over 50;" +
                        " Validation error at 'number2', value cannot be over 50; ");
    }
}
