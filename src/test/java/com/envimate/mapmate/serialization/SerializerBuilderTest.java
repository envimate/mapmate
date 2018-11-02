/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.mapmate.serialization;

import com.envimate.mapmate.domain.scannable.AScannableComplexType;
import com.envimate.mapmate.domain.scannable.AScannableNumber;
import com.envimate.mapmate.domain.scannable.AScannableString;
import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import com.envimate.mapmate.serialization.methods.SerializationMethodNotCompatibleException;
import com.envimate.mapmate.validators.CustomTypeValidationException;
import com.google.gson.Gson;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.matchers.SerializableDefinitionsContainsCustomPrimitiveMatcher.containsValidCustomPrimitiveForType;
import static com.envimate.mapmate.matchers.SerializableDefinitionsContainsDataTransferObjectMatcher.containsValidSerializableDTOForType;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public final class SerializerBuilderTest {

    @Test
    public void givenValidDataTransferObject_whenBuildingWithDataTransferObject_thenReturnsCorrectSerializer() {
        //given
        final Class<?> given = AComplexType.class;

        //when
        final Serializer result = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .withDataTransferObject(given)
                .serializedByItsPublicFields()
                .withCustomPrimitive(AString.class)
                .serializedUsingTheMethodNamed("internalValueForMapping")
                .withCustomPrimitive(ANumber.class)
                .serializedUsingTheMethodNamed("internalValueForMapping")
                .build();

        //then
        assertThat(result.getDefinitions().countDataTransferObjects(), is(equalTo(1)));
        assertThat(result.getDefinitions().countCustomPrimitives(), is(equalTo(2)));
        assertThat(result.getDefinitions(), containsValidSerializableDTOForType(AComplexType.class));
    }

    @Test
    public void givenNull_whenBuildingWithDataTransferObject_thenThrowError() {
        //given
        final Class<?> given = null;
        final String expectedMessage = "type must not be null";

        //when
        try {
            aSerializer()
                    .withDataTransferObject(given)
                    .serializedByItsPublicFields()
                    .build();
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenValidCustomPrimitive_whenBuildingWithCustomPrimitive_thenReturnsCorrectDeserializer() {
        //given
        final Class<?> given = AString.class;

        //when
        final Serializer result = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .withCustomPrimitive(given)
                .serializedUsingTheMethodNamed("internalValueForMapping")
                .build();

        //then
        assertThat(result.getDefinitions().countCustomPrimitives(), is(CoreMatchers.equalTo(1)));
        assertThat(result.getDefinitions().countDataTransferObjects(), is(CoreMatchers.equalTo(0)));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AString.class));
    }

    @Test
    public void givenCustomPrimitiveWithWrongMethod_whenBuildingWithCustomPrimitive_thenThrowError() {
        //given
        final Class<?> given = AString.class;
        final String expectedMessage = "class 'com.envimate.mapmate.domain.valid.AString' does not have a zero " +
                "argument String method named 'fromString'";

        //when
        try {
            aSerializer()
                    .withCustomPrimitive(given)
                    .serializedUsingTheMethodNamed("fromString")
                    .build();
            fail("should throw IllegalArgumentException");
        } catch (final SerializationMethodNotCompatibleException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenCustomPrimitiveWithEmptyMethod_whenBuildingWithCustomPrimitive_thenThrowError() {
        //given
        final Class<?> given = AString.class;
        final String expectedMessage = "methodName must not be empty";

        //when
        try {
            aSerializer()
                    .withCustomPrimitive(given)
                    .serializedUsingTheMethodNamed("")
                    .build();
            fail("should throw IllegalArgumentException");
        } catch (final IllegalArgumentException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenCustomPrimitiveWithNullMethod_whenBuildingWithCustomPrimitive_thenThrowError() {
        //given
        final Class<?> given = AString.class;
        final String expectedMessage = "methodName must not be empty";

        //when
        try {
            aSerializer()
                    .withCustomPrimitive(given)
                    .serializedUsingTheMethodNamed(null)
                    .build();
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNull_whenBuildingWithCustomPrimitive_thenThrowError() {
        //given
        final Class<?> given = null;
        final String expectedMessage = "type must not be null";

        //when
        try {
            aSerializer()
                    .withCustomPrimitive(given)
                    .serializedUsingTheMethodNamed("internalValueForMapping")
                    .build();
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenScannablePackage_whenBuilding_thenReturnCorrectSerializer() {
        //when
        final String given = "com.envimate.mapmate.domain.scannable";

        //when
        final Serializer result = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage(given)
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage(given)
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping")))
                .thatAre().serializedByItsPublicFields()
                .build();

        //then
        assertThat(result.getDefinitions().countCustomPrimitives(), is(CoreMatchers.equalTo(2)));
        assertThat(result.getDefinitions().countDataTransferObjects(), is(CoreMatchers.equalTo(2)));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AScannableString.class));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AScannableNumber.class));
        assertThat(result.getDefinitions(), containsValidSerializableDTOForType(AScannableComplexType.class));
    }

    @Test
    public void givenEmptyPackage_whenBuilding_thenThrowsError() {
        //when
        final String given = "";
        final String expectedMessage = "packageName must not be empty";

        //when
        try {
            aSerializer()
                    .withMarshaller(new Gson()::toJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(includingAll())
                    .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(includingAll())
                    .thatAre().serializedByItsPublicFields()
                    .build();
            fail("should throw IllegalArgumentException");
        } catch (final IllegalArgumentException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNullPackage_whenBuilding_thenThrowsError() {
        //when
        final String given = null;
        final String expectedMessage = "packageName must not be empty";

        //when
        try {
            final Serializer result = aSerializer()
                    .withMarshaller(new Gson()::toJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(includingAll())
                    .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(includingAll())
                    .thatAre().serializedByItsPublicFields()
                    .build();
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenExcludingDataTransferObjectWithScannablePackage_whenBuilding_thenReturnsDeserializerWithoutExcludedDefinition() {
        //when
        final String given = "com.envimate.mapmate.domain.scannable";

        //when
        final Serializer result = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage(given)
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage(given)
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping")))
                .excluding(AScannableComplexType.class)
                .thatAre().serializedByItsPublicFields()
                .build();

        //then
        assertThat(result.getDefinitions().countCustomPrimitives(), is(CoreMatchers.equalTo(2)));
        assertThat(result.getDefinitions().countDataTransferObjects(), is(CoreMatchers.equalTo(1)));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AScannableString.class));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AScannableNumber.class));
        assertThat(result.getDefinitions(), not(containsValidSerializableDTOForType(AScannableComplexType.class)));
    }

    @Test
    public void givenExcludingNullWithScannablePackage_whenBuilding_thenThrowsException() {
        //when
        final String given = "com.envimate.mapmate.domain.scannable";
        final String expectedMessage = "excluded must not be null";

        //when
        try {
            aSerializer()
                    .withMarshaller(new Gson()::toJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                    .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(includingAll())
                    .excluding(null)
                    .thatAre().serializedByItsPublicFields()
                    .build();
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }
}
