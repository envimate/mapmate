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

package com.envimate.mapmate.specs;

import com.envimate.mapmate.domain.valid.*;
import org.junit.Test;

import java.util.Arrays;

import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.specs.givenwhenthen.Given.givenTheExampleMapMateWithAllMarshallers;

public final class SerializerSpecs {

    @Test
    public void testMethodReferenceInCPSerializationMethod() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(
                AComplexType.deserialize(
                        AString.fromStringValue("asdf"),
                        AString.fromStringValue("qwer"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(5555))).withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"stringA\": \"asdf\",\n" +
                        "  \"stringB\": \"qwer\",\n" +
                        "  \"number1\": \"1\",\n" +
                        "  \"number2\": \"5555\"\n" +
                        "}");
    }

    @Test
    public void givenStringDomain_whenSerializing_thenReturnsJsonString() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(AString.fromStringValue("test@test.test")).withMarshallingType(json())
                .theSerializationResultWas("\"test@test.test\"");
    }

    @Test
    public void givenNumberDomain_whenSerializing_thenReturnsJsonString() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(ANumber.fromInt(123)).withMarshallingType(json())
                .theSerializationResultWas("\"123\"");
    }

    @Test
    public void givenComplexDomain_whenSerializing_thenReturnsJsonString() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(
                AComplexType.deserialize(
                        AString.fromStringValue("a"),
                        AString.fromStringValue("b"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(2)))
                .withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"stringA\": \"a\",\n" +
                        "  \"stringB\": \"b\",\n" +
                        "  \"number1\": \"1\",\n" +
                        "  \"number2\": \"2\"\n" +
                        "}");
    }

    @Test
    public void givenComplexDomainWithCollections_whenSerializing_thenReturnsJsonString() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(AComplexTypeWithCollections.deserialize(
                Arrays.asList(AString.fromStringValue("a"),
                        AString.fromStringValue("b"),
                        AString.fromStringValue("c")),
                new ANumber[]{
                        ANumber.fromInt(1),
                        ANumber.fromInt(2),
                        ANumber.fromInt(3),
                }))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"array\": [\n" +
                        "    \"1\",\n" +
                        "    \"2\",\n" +
                        "    \"3\"\n" +
                        "  ],\n" +
                        "  \"arrayList\": [\n" +
                        "    \"a\",\n" +
                        "    \"b\",\n" +
                        "    \"c\"\n" +
                        "  ]\n" +
                        "}");
    }

    @Test
    public void givenComplexNestedDomain_whenSerializing_thenReturnsJsonString() {

        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(AComplexNestedType.deserialize(
                AComplexType.deserialize(
                        AString.fromStringValue("a"),
                        AString.fromStringValue("b"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(2)),
                AComplexType.deserialize(
                        AString.fromStringValue("c"),
                        AString.fromStringValue("d"),
                        ANumber.fromInt(3),
                        ANumber.fromInt(4)))).withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"complexType1\": {\n" +
                        "    \"stringA\": \"a\",\n" +
                        "    \"stringB\": \"b\",\n" +
                        "    \"number1\": \"1\",\n" +
                        "    \"number2\": \"2\"\n" +
                        "  },\n" +
                        "  \"complexType2\": {\n" +
                        "    \"stringA\": \"c\",\n" +
                        "    \"stringB\": \"d\",\n" +
                        "    \"number1\": \"3\",\n" +
                        "    \"number2\": \"4\"\n" +
                        "  }\n" +
                        "}");
    }

    @Test
    public void givenNull_whenSerializing_thenThrowsError() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(null).withMarshallingType(json())
                .anExceptionIsThrownWithAMessageContaining("object must not be null");
    }

    @Test
    public void givenNonConfiguredComplexDomain_whenSerializing_thenThrowsError() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(new ANonConfiguredDomain()).withMarshallingType(json())
                .anExceptionIsThrownWithAMessageContaining(
                        "no definition found for type 'com.envimate.mapmate.specs.SerializerSpecs$ANonConfiguredDomain'");
    }

    @Test
    public void givenCyclicType_whenSerializing_thenThrowsError() {
        final ACyclicType given1 = ACyclicType.deserialize(AString.fromStringValue("a"));
        final ACyclicType given2 = ACyclicType.deserialize(AString.fromStringValue("b"));
        given1.aCyclicType = given2;
        given2.aCyclicType = given1;

        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(given1).withMarshallingType(json())
                .anExceptionIsThrownWithAMessageContaining("a circular reference has been detected for objects " +
                        "of type com.envimate.mapmate.domain.valid.ACyclicType");
    }

    @Test
    public void givenNonCyclicType_whenSerializing_thenDoesNotThrowsError() {
        final ACyclicType given1 = ACyclicType.deserialize(AString.fromStringValue("a"));
        final ACyclicType given2 = ACyclicType.deserialize(AString.fromStringValue("b"));
        final ACyclicType given3 = ACyclicType.deserialize(AString.fromStringValue("c"));

        given1.aCyclicType = given2;
        given2.aCyclicType = given3;

        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(given1).withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"aCyclicType\": {\n" +
                        "    \"aCyclicType\": {\n" +
                        "      \"aString\": \"c\"\n" +
                        "    },\n" +
                        "    \"aString\": \"b\"\n" +
                        "  },\n" +
                        "  \"aString\": \"a\"\n" +
                        "}");
    }

    @Test
    public void givenComplexDomainWithNullValues_whenSerializing_thenExcludesFromJson() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(
                AComplexType.deserialize(
                        AString.fromStringValue("a"),
                        null,
                        ANumber.fromInt(1),
                        null)).withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"stringA\": \"a\",\n" +
                        "  \"number1\": \"1\"\n" +
                        "}");
    }

    @Test
    public void givenComplexDomainUsingInjector_whenSerializing_thenReturnsJsonString() {

        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializesWithInjector(
                AComplexType.deserialize(
                        AString.fromStringValue("a"),
                        AString.fromStringValue("b"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(2)),
                input -> {
                    input.put("stringA", "test");
                    return input;
                })
                .withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"stringA\": \"test\",\n" +
                        "  \"stringB\": \"b\",\n" +
                        "  \"number1\": \"1\",\n" +
                        "  \"number2\": \"2\"\n" +
                        "}");
    }

    public static class ANonConfiguredDomain {
    }
}
