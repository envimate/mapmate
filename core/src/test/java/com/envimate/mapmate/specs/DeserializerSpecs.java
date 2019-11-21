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

import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.specs.givenwhenthen.Given.givenTheExampleMapMateWithAllMarshallers;

public final class DeserializerSpecs {

    @Test
    public void givenStringJson_whenDeserializing_thenReturnAStringObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("\"string with special symbols like \' \"").as(json()).toTheType(AString.class)
                .theDeserializedObjectIs(AString.fromStringValue("string with special symbols like ' "));
    }

    @Test
    public void givenNumberJson_whenDeserializing_thenReturnANumberObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("49").as(json()).toTheType(ANumber.class)
                .theDeserializedObjectIs(ANumber.fromInt(49));
    }

    @Test
    public void givenComplexTypeJson_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}")
                .as(json()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(AComplexType.deserialize(
                        AString.fromStringValue("a"),
                        AString.fromStringValue("b"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(2)
                ));
    }

    @Test
    public void givenComplexTypeWithArray_whenDeserializing_thenReturnObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"array\":[\"1\", \"2\", \"3\"]}")
                .as(json()).toTheType(AComplexTypeWithArray.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(AComplexTypeWithArray.deserialize(
                        new ANumber[]{ANumber.fromInt(1), ANumber.fromInt(2), ANumber.fromInt(3)})
                );
    }

    @Test
    public void givenComplexTypeWithInvalidArray_whenDeserializing_thenThrowCorrectException() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"array\":[\"1\", \"51\", \"53\"]}")
                .as(json()).toTheType(AComplexTypeWithArray.class)
                .anAggregatedExceptionHasBeenThrownWithNumberOfErrors(2);
    }

    @Test
    public void givenComplexNestedTypeJson_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("" +
                "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                "}")
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("a"),
                                AString.fromStringValue("b"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("c"),
                                AString.fromStringValue("d"),
                                ANumber.fromInt(3),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void givenNull_whenDeserializing_thenThrowsError() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes(null).as(json()).toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("input must not be null");
    }

    @Test
    public void givenEmpty_whenDeserializing_thenReturnsNull() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("").as(json()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(null)
                .noExceptionHasBeenThrown();
    }

    @Test
    public void givenInvalidJson_whenDeserializing_thenThrowsError() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"number1\";\"1\",\"number2\":\"2\",\"stringA\"=\"a\",\"stringB\":\"b\"}")
                .as(json()).toTheType(AComplexType.class)
                .anExceptionIsThrownWithAMessageContaining("Could not unmarshal map from input {" +
                        "\"number1\";\"1\"," +
                        "\"number2\":\"2\"," +
                        "\"stringA\"=\"a\"," +
                        "\"stringB\":\"b\"}");
    }

    @Test
    public void givenIncompleteJson_whenDeserializing_thenFillsWithNull() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"number1\":\"1\",\"stringA\":\"a\"}")
                .as(json()).toTheType(AComplexType.class)
                .theDeserializedObjectIs(AComplexType.deserialize(
                        AString.fromStringValue("a"),
                        null,
                        ANumber.fromInt(1),
                        null
                ));
    }

    @Test
    public void givenJsonWithValidValues_whenDeserializing_thenReturnsObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"number1\":\"21\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}")
                .as(json()).toTheType(AComplexTypeWithValidations.class)
                .theDeserializedObjectIs(AComplexTypeWithValidations.deserialize(
                        AString.fromStringValue("a"),
                        AString.fromStringValue("b"),
                        ANumber.fromInt(21),
                        ANumber.fromInt(2)
                ));
    }

    @Test
    public void givenJsonWithNestedValidationExceptions_whenDeserializing_thenReturnsOnlyOneValidationException() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{\"node\": {\"leaf\":\"1234\"}}")
                .as(json()).toTheType(AComplexNestedValidatedType.class)
                .anAggregatedExceptionHasBeenThrownWithNumberOfErrors(1);
    }

    @Test
    public void deserializerCanFindFactoryMethodsWithArrays() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("{array: [\"1\"]}").as(json()).toTheType(AComplexTypeWithListButArrayConstructor.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(AComplexTypeWithListButArrayConstructor.deserialize(new ANumber[]{ANumber.fromInt(1)}));
    }

    @Test
    public void nestedCollectionsCanBeDeserialized() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializes("" +
                "{\n" +
                "  \"nestedList\": [\n" +
                "    [\n" +
                "      [\n" +
                "        [\n" +
                "          \"42\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"nestedArray\": [\n" +
                "    [\n" +
                "      [\n" +
                "        [\n" +
                "          \"arrays\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"nestedMix2\": [\n" +
                "    [\n" +
                "      [\n" +
                "        [\n" +
                "          \"43\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"nestedMix1\": [\n" +
                "    [\n" +
                "      [\n" +
                "        [\n" +
                "          \"mixed\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    ]\n" +
                "  ]\n" +
                "}").as(json()).toTheType(AComplexTypeWithNestedCollections.class)
                .noExceptionHasBeenThrown()
                .theDeserialiedObjectHas(AComplexTypeWithNestedCollections.class, result -> result.nestedArray[0][0][0][0].equals(AString.fromStringValue("arrays")))
                .theDeserialiedObjectHas(AComplexTypeWithNestedCollections.class, result -> result.nestedList.get(0).get(0).get(0).get(0).equals(ANumber.fromInt(42)))
                .theDeserialiedObjectHas(AComplexTypeWithNestedCollections.class, result -> result.nestedMix1[0].get(0)[0].get(0).equals(AString.fromStringValue("mixed")))
                .theDeserialiedObjectHas(AComplexTypeWithNestedCollections.class, result -> result.nestedMix2.get(0)[0].get(0)[0].equals(ANumber.fromInt(43)));
    }
}
