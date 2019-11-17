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

import org.junit.Test;

import static com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncoded;
import static com.envimate.mapmate.specs.givenwhenthen.Given.givenTheExampleMapMateWithAllMarshallers;
import static com.envimate.mapmate.specs.instances.Instances.*;
import static com.envimate.mapmate.marshalling.MarshallingType.*;

public final class MarshallerSpecs {

    @Test
    public void testJsonMarshallingIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDto()).withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"stringA\": \"asdf\",\n" +
                        "  \"stringB\": \"qwer\",\n" +
                        "  \"number1\": \"1\",\n" +
                        "  \"number2\": \"5\"\n" +
                        "}");
    }

    @Test
    public void testJsonMarshallingWithCollectionsIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDtoWithCollections()).withMarshallingType(json())
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"array\": [\n" +
                        "    \"1\",\n" +
                        "    \"2\"\n" +
                        "  ]\n" +
                        "}");
    }

    @Test
    public void testXmlMarshallingIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDto()).withMarshallingType(xml())
                .theSerializationResultWas("" +
                        "<HashMap>\n" +
                        "  <stringA>asdf</stringA>\n" +
                        "  <stringB>qwer</stringB>\n" +
                        "  <number1>1</number1>\n" +
                        "  <number2>5</number2>\n" +
                        "</HashMap>\n");
    }

    @Test
    public void testYamlMarshallingIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDto()).withMarshallingType(yaml())
                .theSerializationResultWas("" +
                        "stringA: asdf\n" +
                        "stringB: qwer\n" +
                        "number1: '1'\n" +
                        "number2: '5'\n");
    }

    @Test
    public void testUrlEncodedMarshallingIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDto()).withMarshallingType(urlEncoded())
                .theSerializationResultWas("stringA=asdf&stringB=qwer&number1=1&number2=5");
    }

    @Test
    public void testUrlEncodedMarshallingWithCollectionsIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDtoWithCollections()).withMarshallingType(urlEncoded())
                .theSerializationResultWas("array[0]=1&array[1]=2");
    }

    @Test
    public void testUrlEncodedMarshallingWithMapsIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedNestedExampleDto()).withMarshallingType(urlEncoded())
                .theSerializationResultWas("" +
                        "complexType1[stringA]=a&" +
                        "complexType1[stringB]=b&" +
                        "complexType1[number1]=1&" +
                        "complexType1[number2]=2&" +
                        "complexType2[stringA]=c&" +
                        "complexType2[stringB]=d&" +
                        "complexType2[number1]=3&" +
                        "complexType2[number2]=4");
    }

    @Test
    public void testUnknownMarshallerThrowsAnException() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateSerializes(theFullyInitializedExampleDto()).withMarshallingType(marshallingType("unknown"))
                .anExceptionIsThrownWithAMessageContaining(
                        "Unsupported marshalling type 'unknown'," +
                                " known marshalling types are: ['urlencoded', 'json', 'yaml', 'xml']");
    }
}
