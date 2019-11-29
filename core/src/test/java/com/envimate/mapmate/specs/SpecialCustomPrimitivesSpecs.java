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

import com.envimate.mapmate.builder.detection.customprimitive.mapping.BooleanFormatException;
import com.envimate.mapmate.domain.valid.*;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;
import static com.envimate.mapmate.specs.givenwhenthen.Marshallers.jsonMarshaller;
import static com.envimate.mapmate.specs.givenwhenthen.Unmarshallers.jsonUnmarshaller;

public final class SpecialCustomPrimitivesSpecs {

    @Test
    public void doubleBasedCustomPrimitivesCanBeDeserialized() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithDoublesDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"doubleA\": 1, \"doubleB\": 2}").as(json()).toTheType(AComplexTypeWithDoublesDto.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(new AComplexTypeWithDoublesDto(new APrimitiveDouble(1.0), new AWrapperDouble(2.0)));
    }

    @Test
    public void doubleBasedCustomPrimitivesCanBeDeserializedWithStrings() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithDoublesDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"doubleA\": \"1\", \"doubleB\": \"2\"}").as(json()).toTheType(AComplexTypeWithDoublesDto.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(new AComplexTypeWithDoublesDto(new APrimitiveDouble(1.0), new AWrapperDouble(2.0)));
    }

    @Test
    public void doubleBasedCustomPrimitivesCanNotBeDeserializedWithWrongStrings() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithDoublesDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .withExceptionIndicatingValidationError(NumberFormatException.class)
                        .build()
        )
                .when().mapMateDeserializes("{\"doubleA\": \"foo\", \"doubleB\": \"bar\"}").as(json()).toTheType(AComplexTypeWithDoublesDto.class)
                .anAggregatedExceptionHasBeenThrownWithNumberOfErrors(2);
    }

    @Test
    public void doubleBasedCustomPrimitivesCanBeSerialized() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithDoublesDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(new AComplexTypeWithDoublesDto(new APrimitiveDouble(1.0), new AWrapperDouble(2.0)))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"doubleB\": 2.0,\n" +
                        "  \"doubleA\": 1.0" +
                        "\n}");
    }

    @Test
    public void booleanBasedCustomPrimitivesCanBeDeserialized() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithBooleansDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"booleanA\": true, \"booleanB\": false}").as(json()).toTheType(AComplexTypeWithBooleansDto.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(new AComplexTypeWithBooleansDto(new APrimitiveBoolean(true), new AWrapperBoolean(false)));
    }

    @Test
    public void booleanBasedCustomPrimitivesCanBeDeserializedWithStrings() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithBooleansDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"booleanA\": \"true\", \"booleanB\": \"false\"}").as(json()).toTheType(AComplexTypeWithBooleansDto.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(new AComplexTypeWithBooleansDto(new APrimitiveBoolean(true), new AWrapperBoolean(false)));
    }

    @Test
    public void booleanBasedCustomPrimitivesCanNotBeDeserializedWithWrongStrings() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithBooleansDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .withExceptionIndicatingValidationError(BooleanFormatException.class)
                        .build()
        )
                .when().mapMateDeserializes("{\"booleanA\": \"foo\", \"booleanB\": \"bar\"}").as(json()).toTheType(AComplexTypeWithBooleansDto.class)
                .anAggregatedExceptionHasBeenThrownWithNumberOfErrors(2);
    }

    @Test
    public void booleanBasedCustomPrimitivesCanBeSerialized() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithBooleansDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(new AComplexTypeWithBooleansDto(new APrimitiveBoolean(true), new AWrapperBoolean(false)))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"booleanB\": false,\n" +
                        "  \"booleanA\": true\n" +
                        "}");
    }

    @Test
    public void integerBasedCustomPrimitivesCanBeDeserialized() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithIntegersDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"intA\": 1, \"intB\": 2}").as(json()).toTheType(AComplexTypeWithIntegersDto.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(new AComplexTypeWithIntegersDto(new APrimitiveInteger(1), new AWrapperInteger(2)));
    }

    @Test
    public void integerBasedCustomPrimitivesCanBeDeserializedWithStrings() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithIntegersDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"intA\": \"1\", \"intB\": \"2\"}").as(json()).toTheType(AComplexTypeWithIntegersDto.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(new AComplexTypeWithIntegersDto(new APrimitiveInteger(1), new AWrapperInteger(2)));
    }

    @Test
    public void integerBasedCustomPrimitivesCanBeSerialized() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexTypeWithIntegersDto.class)
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(new AComplexTypeWithIntegersDto(new APrimitiveInteger(1), new AWrapperInteger(2)))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"intB\": 2.0,\n" +
                        "  \"intA\": 1.0\n" +
                        "}");
    }
}
