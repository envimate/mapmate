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

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import org.junit.Test;

import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;
import static com.envimate.mapmate.specs.givenwhenthen.MapMateInstances.theExampleMapMateWithAllMarshallers;

public final class PerformanceSpecs {

    @Test
    public void aLotOfSerializationsDoNotCauseProblems() {
        final MapMate mapMate = theExampleMapMateWithAllMarshallers();

        for (int i = 0; i < 10_000_000; ++i) {
            given(mapMate)
                    .when().mapMateSerializes(
                    AComplexType.deserialize(
                            AString.fromStringValue("asdf"),
                            AString.fromStringValue("qwer"),
                            ANumber.fromInt(1),
                            ANumber.fromInt(5)))
                    .withMarshallingType(json())
                    .theSerializationResultWas("" +
                            "{\n" +
                            "  \"number1\": \"1\",\n" +
                            "  \"number2\": \"5\",\n" +
                            "  \"stringA\": \"asdf\",\n" +
                            "  \"stringB\": \"qwer\"\n" +
                            "}");
        }
    }

    @Test
    public void aLotOfDeserializationsDoNotCauseProblems() {
        final MapMate mapMate = theExampleMapMateWithAllMarshallers();

        for (int i = 0; i < 10_000_000; ++i) {
            given(mapMate)
                    .when().mapMateDeserializes("" +
                    "{\n" +
                    "  \"number1\": \"1\",\n" +
                    "  \"number2\": \"5\",\n" +
                    "  \"stringA\": \"asdf\",\n" +
                    "  \"stringB\": \"qwer\"\n" +
                    "}")
                    .as(json()).toTheType(AComplexType.class)
                    .noExceptionHasBeenThrown()
                    .theDeserializedObjectIs(AComplexType.deserialize(
                            AString.fromStringValue("asdf"),
                            AString.fromStringValue("qwer"),
                            ANumber.fromInt(1),
                            ANumber.fromInt(5)));
        }
    }
}
