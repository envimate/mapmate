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
import static java.util.Collections.singletonList;

public final class InjectionSpecs {

    @Test
    public void givenComplexTypeJsonWithInjectorUsingPropertyNameAndInstance_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{" +
                        "\"complexType1\":" +
                        "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                        "\"complexType2\":" +
                        "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                        "}",
                injector -> injector
                        .put("complexType1.stringB", AString.fromStringValue("test"))
                        .put("complexType2.number1", ANumber.fromStringValue("45")))
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("a"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("c"),
                                AString.fromStringValue("d"),
                                ANumber.fromInt(45),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingInstance_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{" +
                        "\"complexType1\":" +
                        "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                        "\"complexType2\":" +
                        "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                        "}",
                injector -> injector
                        .put(AString.fromStringValue("test"))
                        .put(AString.fromStringValue("test")))
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(3),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingInstanceAndType_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{" +
                        "\"complexType1\":" +
                        "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                        "\"complexType2\":" +
                        "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                        "}",
                injector -> injector.put(AString.class, AString.fromStringValue("test")))
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(3),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void givenComplexTypeWithIncompleteJsonWithInjectorUsingPropertyPath_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{" +
                        "\"complexType1\":" +
                        "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                        "\"complexType2\":" +
                        "{\"number1\":\"3\",\"number2\":\"4\"}" +
                        "}",
                injector -> injector.put("complexType2.stringA", AString.fromStringValue("test")))
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("a"),
                                AString.fromStringValue("b"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                null,
                                ANumber.fromInt(3),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void givenComplexTypeWithIncompleteJsonWithInjectorUsingInstance_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{" +
                        "\"complexType1\":" +
                        "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                        "\"complexType2\":" +
                        "{\"number1\":\"3\",\"number2\":\"4\"}" +
                        "}",
                injector -> injector.put(AString.fromStringValue("test"))
        )
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("test"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(3),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingPropertyNameAndStringValue_whenDeserializing_thenReturnAComplexObject() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{" +
                        "\"complexType1\":" +
                        "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                        "\"complexType2\":" +
                        "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                        "}",
                injector -> injector
                        .put("complexType1.stringB", "test")
                        .put("complexType2.number1", "45")
        )
                .as(json()).toTheType(AComplexNestedType.class)
                .theDeserializedObjectIs(AComplexNestedType.deserialize(
                        AComplexType.deserialize(
                                AString.fromStringValue("a"),
                                AString.fromStringValue("test"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2)
                        ),
                        AComplexType.deserialize(
                                AString.fromStringValue("c"),
                                AString.fromStringValue("d"),
                                ANumber.fromInt(45),
                                ANumber.fromInt(4)
                        )
                ));
    }

    @Test
    public void universalInjectionIntoCollectionIsPossible() {
        givenTheExampleMapMateWithAllMarshallers()
                .when().mapMateDeserializesWithInjection("{ \"arrayList\": [\"not_injected\"], \"array\": [\"1\"] }",
                injector -> {
                    injector.put("arrayList.[0]", "injected");
                    injector.put("array.[0]", "42");
                })
                .as(json()).toTheType(AComplexTypeWithCollections.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(
                        AComplexTypeWithCollections.deserialize(
                                singletonList(AString.fromStringValue("injected")),
                                new ANumber[]{ANumber.fromInt(42)}
                        ));
    }
}
