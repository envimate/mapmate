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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.deserialization.methods.DeserializationMethodNotCompatibleException;
import com.envimate.mapmate.domain.invalid.AComplexTypeWithMultipleFactoryMethods;
import com.envimate.mapmate.domain.invalid.AComplexTypeWithoutFactoryMethods;
import com.envimate.mapmate.domain.scannable.AScannableString;
import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.AComplexTypeWithMap;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import com.envimate.mapmate.reflections.FactoryMethodNotFoundException;
import com.envimate.mapmate.reflections.MultipleFactoryMethodsException;
import com.envimate.mapmate.validators.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.matchers.DeserializableDefinitionsContainsCustomPrimitiveMatcher.containsValidCustomPrimitiveForType;
import static com.envimate.mapmate.matchers.DeserializableDefinitionsContainsDataTransferObjectMatcher.containsValidDeserializableDTOForType;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@SuppressWarnings("deprecation")
public final class DeserializerBuilderTest {

    @Test
    public void givenValidDataTransferObject_whenBuildingWithDataTransferObject_thenReturnsCorrectDeserializer() {
        final Class<?> given = AComplexType.class;
        final Deserializer result = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .withDataTransferObject(given)
                .deserializedUsingTheSingleFactoryMethod()
                .withCustomPrimitive(AString.class)
                .deserializedUsingTheMethodNamed("fromString")
                .withCustomPrimitive(ANumber.class)
                .deserializedUsingTheMethodNamed("fromString")
                .build();
        assertThat(result.getDefinitions().countCustomPrimitives(), is(equalTo(3)));
        assertThat(result.getDefinitions().countDataTransferObjects(), is(equalTo(1)));
        assertThat(result.getDefinitions(), containsValidDeserializableDTOForType(AComplexType.class));
        final Definition definition = result.getDefinitions().getDefinitionForType(AComplexType.class).get();
        final DeserializableDataTransferObject<?> dto = (DeserializableDataTransferObject) definition;
        assertThat(dto.elements().referencedTypes().size(), is(equalTo(2)));
        assertThat(dto.elements().referencedTypes(), hasItem(AString.class));
        assertThat(dto.elements().referencedTypes(), hasItem(ANumber.class));
    }

    @Test
    public void givenDataTransferObjectWithoutFactoryMethods_whenBuildingWithDataTransferObject_thenThrowsException() {
        final Class<?> given = AComplexTypeWithoutFactoryMethods.class;
        final String expectedMessage = "no factory method found " +
                "on type 'class com.envimate.mapmate.domain.invalid.AComplexTypeWithoutFactoryMethods'";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withDataTransferObject(given)
                    .deserializedUsingTheSingleFactoryMethod()
                    .validateNoUnsupportedOutgoingReferences()
                    .build();
            fail("should throw FactoryMethodNotFoundException");
        } catch (final FactoryMethodNotFoundException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenDataTransferObjectWithMultipleFactoryMethods_whenBuildingWithDataTransferObject_thenThrowsException() {
        final Class<?> given = AComplexTypeWithMultipleFactoryMethods.class;
        final String expectedMessage = "multiple factory methods found " +
                "for type 'com.envimate.mapmate.domain.invalid.AComplexTypeWithMultipleFactoryMethods'";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withDataTransferObject(given)
                    .deserializedUsingTheSingleFactoryMethod()
                    .validateNoUnsupportedOutgoingReferences()
                    .build();
            fail("should throw MultipleFactoryMethodsException");
        } catch (final MultipleFactoryMethodsException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void givenDataTransferObjectWithAdapterMethod_whenBuildingWithDataTransferObject_thenReturnsCorrectDesererializer() {
        final Class<?> givenType = AComplexType.class;
        final DeserializationDTOMethod given = new DeserializationDTOMethod() {
            @Override
            public Object deserialize(Class<?> targetType, Map<String, Object> elements) throws Exception {
                return null;
            }

            @Override
            public Map<String, Class<?>> elements(Class<?> targetType) {
                return new HashMap<>();
            }
        };
        final Deserializer result = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .withDataTransferObject(givenType)
                .deserializedUsing(given)
                .build();
        assertThat(result.getDefinitions().countDataTransferObjects(), is(equalTo(1)));
        assertThat(result.getDefinitions(), containsValidDeserializableDTOForType(AComplexType.class));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void givenDataTransferObjectWithNullAdapterMethod_whenBuildingWithDataTransferObject_thenThrowsError() {
        final Class<?> givenType = AComplexType.class;
        final DeserializationDTOMethod givenAdapter = null;
        final String expectedMessage = "method must not be null";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withDataTransferObject(givenType)
                    .deserializedUsing(givenAdapter)
                    .build();
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNull_whenBuildingWithDataTransferObject_thenThrowError() {
        final Class<?> given = null;
        final String expectedMessage = "type must not be null";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withDataTransferObject(given)
                    .deserializedUsingTheSingleFactoryMethod()
                    .build();
            fail("should throw Exception");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenValidCustomPrimitive_whenBuildingWithCustomPrimitive_thenReturnsCorrectDeserializer() {
        final Class<?> given = AString.class;
        final Deserializer result = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .withCustomPrimitive(given)
                .deserializedUsingTheMethodNamed("fromString")
                .build();
        assertThat(result.getDefinitions().countCustomPrimitives(), is(equalTo(2)));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AString.class));
    }

    @Test
    public void givenCustomPrimitiveWithWrongMethod_whenBuildingWithCustomPrimitive_thenThrowError() {
        final Class<?> given = AString.class;
        final String expectedMessage = "class 'com.envimate.mapmate.domain.valid.AString' does not have a static method" +
                " with a single String argument named 'nonexistingmethod'";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withCustomPrimitive(given)
                    .deserializedUsingTheMethodNamed("nonexistingmethod")
                    .build();
            fail("should throw Exception");
        } catch (final DeserializationMethodNotCompatibleException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenCustomPrimitiveWithEmptyMethod_whenBuildingWithCustomPrimitive_thenThrowError() {
        final Class<?> given = AString.class;
        final String expectedMessage = "methodName must not be empty";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withCustomPrimitive(given)
                    .deserializedUsingTheMethodNamed("")
                    .build();
            fail("should throw IllegalArgumentException");
        } catch (final IllegalArgumentException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenCustomPrimitiveWithNullMethod_whenBuildingWithCustomPrimitive_thenThrowError() {
        final Class<?> given = AString.class;
        final String expectedMessage = "methodName must not be empty";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withCustomPrimitive(given)
                    .deserializedUsingTheMethodNamed(null)
                    .build();
            fail("should throw Exception");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNull_whenBuildingWithCustomPrimitive_thenThrowError() {
        final Class<?> given = null;
        final String expectedMessage = "type must not be null";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .withCustomPrimitive(given)
                    .deserializedUsingTheMethodNamed("fromString")
                    .build();
            fail("should throw Exception");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenEmptyPackage_whenBuilding_thenThrowsError() {
        final String given = "";
        final String expectedMessage = "packageName must not be empty";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(includingAll())
                    .thatAre().deserializedUsingTheMethodNamed("fromString")
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(includingAll())
                    .thatAre().deserializedUsingTheSingleFactoryMethod()
                    .build();
            fail("should throw IllegalArgumentException");
        } catch (final IllegalArgumentException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNullPackage_whenBuilding_thenThrowsError() {
        final String given = null;
        final String expectedMessage = "packageName must not be empty";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(includingAll())
                    .thatAre().deserializedUsingTheMethodNamed("fromString")
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(includingAll())
                    .thatAre().deserializedUsingTheSingleFactoryMethod()
                    .build();
            fail("should throw NullPointerException");
        } catch (final Exception result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenExcludingCustomPrimitiveWithScannablePackage_whenBuilding_thenThrowsError() {
        final String given = "com.envimate.mapmate.domain.scannable";
        final String expectedMessage = "definitions contain field of unknown type" +
                " com.envimate.mapmate.domain.scannable.AScannableString";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                    .excluding(AScannableString.class)
                    .thatAre().deserializedUsingTheMethodNamed("fromString")
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                    .excluding(AComplexTypeWithMap.class)
                    .excluding(AScannableString.class)
                    .thatAre().deserializedUsingTheSingleFactoryMethod()
                    .validateNoUnsupportedOutgoingReferences()
                    .build();
            fail("should throw UnknownReferenceException");
        } catch (final UnknownReferenceException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenExcludingDataTransferObjectWithScannablePackage_whenBuilding_thenReturnsDeserializerWithoutExcludedDefinition() {
        final String given = "com.envimate.mapmate.domain.scannable";
        final Deserializer result = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage(given)
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheMethodNamed("fromString")
                .thatScansThePackage(given)
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .excluding(AComplexTypeWithMap.class)
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();
        assertThat(result.getDefinitions().countCustomPrimitives(), is(equalTo(3)));
        assertThat(result.getDefinitions().countDataTransferObjects(), is(equalTo(2)));
        assertThat(result.getDefinitions(), containsValidCustomPrimitiveForType(AScannableString.class));
    }

    @Test
    public void givenExcludingNullWithScannablePackage_whenBuilding_thenThrowsException() {
        final String given = "com.envimate.mapmate.domain.scannable";
        final String expectedMessage = "excluded must not be null";
        try {
            aDeserializer()
                    .withJsonUnmarshaller(new Gson()::fromJson)
                    .thatScansThePackage(given)
                    .forCustomPrimitives()
                    .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                    .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                    .thatScansThePackage(given)
                    .forDataTransferObjects()
                    .filteredBy(includingAll())
                    .excluding(null)
                    .thatAre().deserializedUsingTheSingleFactoryMethod()
                    .build();
            fail("should throw Exception");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

}
