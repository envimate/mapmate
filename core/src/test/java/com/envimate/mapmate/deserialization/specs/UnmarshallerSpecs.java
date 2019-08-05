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

import com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe;
import com.envimate.mapmate.domain.valid.AComplexNestedType;
import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.AComplexTypeWithArray;
import org.junit.Test;

import static com.envimate.mapmate.deserialization.specs.givenwhenthen.Given.givenTheExampleMapMateDeserializer;
import static com.envimate.mapmate.deserialization.specs.instances.Instances.*;
import static com.envimate.mapmate.marshalling.MarshallingType.*;

public final class UnmarshallerSpecs {

    @Test
    public void testJsonUnmarshallingIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "{\n" +
                "  \"number1\": \"1\",\n" +
                "  \"number2\": \"5\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").as(json()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(theFullyInitializedExampleDto());
    }

    @Test
    public void testJsonUnmarshallingWithCollectionsIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "{\n" +
                "  \"array\": [\n" +
                "    \"1\",\n" +
                "    \"2\"\n" +
                "  ]\n" +
                "}").as(json()).toTheType(AComplexTypeWithArray.class)
                .theDeserializedObjectIs(theFullyInitializedExampleDtoWithCollections());
    }

    @Test
    public void testXmlUnmarshallingIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "<HashMap>\n" +
                "  <number1>1</number1>\n" +
                "  <number2>5</number2>\n" +
                "  <stringA>asdf</stringA>\n" +
                "  <stringB>qwer</stringB>\n" +
                "</HashMap>\n").as(xml()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(theFullyInitializedExampleDto());
    }

    @Test
    public void testYamlUnmarshallingIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "number1: '1'\n" +
                "number2: '5'\n" +
                "stringA: asdf\n" +
                "stringB: qwer\n").as(yaml()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(theFullyInitializedExampleDto());
    }

    @Test
    public void testUrlEncodedUnmarshallingIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("number1=1&number2=5&stringA=asdf&stringB=qwer")
                .as(UrlEncodedMarshallerRecipe.urlEncoded()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(theFullyInitializedExampleDto());
    }

    @Test
    public void testUrlEncodedUnmarshallingWithCollectionsIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("array[0]=1&array[1]=2")
                .as(UrlEncodedMarshallerRecipe.urlEncoded()).toTheType(AComplexTypeWithArray.class)
                .theDeserializedObjectIs(theFullyInitializedExampleDtoWithCollections());
    }

    @Test
    public void testUrlEncodedUnmarshallingWithMapsIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "complexType2[number1]=3&" +
                "complexType2[number2]=4&" +
                "complexType2[stringA]=c&" +
                "complexType2[stringB]=d&" +
                "complexType1[number1]=1&" +
                "complexType1[number2]=2&" +
                "complexType1[stringA]=a&" +
                "complexType1[stringB]=b").as(UrlEncodedMarshallerRecipe.urlEncoded()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(theFullyInitializedNestedExampleDto());
    }

    @Test
    public void testUnknownUnmarshallerThrowsAnException() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "{\n" +
                "  \"number1\": \"1\",\n" +
                "  \"number2\": \"5\",\n" +
                "  \"stringA\": \"asdf\",\n" +
                "  \"stringB\": \"qwer\"\n" +
                "}").as(marshallingType("unknown")).toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining(
                        "Unsupported marshalling type 'unknown'," +
                                " known marshalling types are: ['urlencoded', 'json', 'xml', 'yaml']");
    }
}
