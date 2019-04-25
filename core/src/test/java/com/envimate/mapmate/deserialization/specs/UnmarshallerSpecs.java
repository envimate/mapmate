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

import org.junit.Test;

import static com.envimate.mapmate.deserialization.specs.givenwhenthen.Given.givenTheExampleMapMateDeserializer;
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
                "}").as(json()).toTheExampleDto()
                .theDeserializedObjectIsTheFullyInitializedExampleDto();

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
                "</HashMap>\n").as(xml()).toTheExampleDto()
                .theDeserializedObjectIsTheFullyInitializedExampleDto();
    }

    @Test
    public void testYamlUnmarshallingIsPossible() {
        givenTheExampleMapMateDeserializer()
                .when().theDeserializerDeserializes("" +
                "number1: '1'\n" +
                "number2: '5'\n" +
                "stringA: asdf\n" +
                "stringB: qwer\n").as(yaml()).toTheExampleDto()
                .theDeserializedObjectIsTheFullyInitializedExampleDto();
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
                "}").as(marshallingType("unknown")).toTheExampleDto()
                .anExceptionIsThrownWithTheMessage(
                        "Unsupported marshalling type 'unknown'," +
                                " known marshalling types are: ['json', 'xml', 'yaml']");
    }
}
