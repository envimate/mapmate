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

import com.envimate.mapmate.domain.half.ADeserializationOnlyComplexType;
import com.envimate.mapmate.domain.half.ADeserializationOnlyString;
import com.envimate.mapmate.domain.half.ASerializationOnlyComplexType;
import com.envimate.mapmate.domain.half.ASerializationOnlyString;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;
import static com.envimate.mapmate.specs.givenwhenthen.Marshallers.jsonMarshaller;
import static com.envimate.mapmate.specs.givenwhenthen.Unmarshallers.jsonUnmarshaller;

public final class HalfDefinitionsSpecs {

    @Test
    public void aCustomPrimitiveCanBeSerializationOnly() {
        given(
                aMapMate()
                        .withManuallyAddedType(ASerializationOnlyString.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(ASerializationOnlyString.init()).withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("\"theValue\"");
    }

    @Test
    public void aCustomPrimitiveCanBeDeserializationOnly() {
        given(
                aMapMate()
                        .withManuallyAddedType(ADeserializationOnlyString.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("\"foo\"").as(json()).toTheType(ADeserializationOnlyString.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(ADeserializationOnlyString.fromStringValue("foo"));
    }

    @Test
    public void aSerializedObjectCanBeSerializationOnly() {
        given(
                aMapMate()
                        .withManuallyAddedType(ASerializationOnlyComplexType.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(ASerializationOnlyComplexType.init()).withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"string\": \"theValue\"\n" +
                        "}");
    }

    @Test
    public void aSerializedObjectCanBeDeserializationOnly() {
        given(
                aMapMate()
                        .withManuallyAddedType(ADeserializationOnlyComplexType.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("" +
                "{\n" +
                "  \"string\": \"foo\"\n" +
                "}")
                .as(json()).toTheType(ADeserializationOnlyComplexType.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(ADeserializationOnlyComplexType.deserialize(ADeserializationOnlyString.fromStringValue("foo")));
    }
}
