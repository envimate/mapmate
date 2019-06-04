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

import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.deserialization.validation.AggregatedValidationException;
import com.envimate.mapmate.deserialization.validation.ValidationError;
import com.envimate.mapmate.domain.valid.*;
import com.envimate.mapmate.validators.CustomTypeValidationException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.mapmate.Defaults.theDefaultDeserializer;
import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.domain.valid.AComplexType.aComplexType;
import static com.envimate.mapmate.domain.valid.ANumber.fromInt;
import static com.envimate.mapmate.domain.valid.AString.fromString;
import static com.envimate.mapmate.domain.valid.AnException.anException;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@SuppressWarnings("CastToConcreteClass")
public final class DeserializerTest {

    private static AString aStaticProviderMethod(final String input) {
        return fromString("test");
    }

    @Test
    public void givenStringJson_whenDeserializing_thenReturnAStringObject() {
        final String given = "\"string with special symbols like \' \"";
        final AString result = theDefaultDeserializer().deserializeJson(given, AString.class);
        assertThat(result.internalValueForMapping(), is(equalTo("string with special symbols like ' ")));
    }

    @Test
    public void givenNumberJson_whenDeserializing_thenReturnANumberObject() {
        final String given = "49";
        final ANumber result = theDefaultDeserializer().deserializeJson(given, ANumber.class);
        assertThat(result.internalValueForMapping(), is(equalTo("49")));
    }

    @Test
    public void givenComplexTypeJson_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final AComplexType result = theDefaultDeserializer().deserializeJson(given, AComplexType.class);
        assertThat(result.stringA.internalValueForMapping(), is(equalTo("a")));
        assertThat(result.stringB.internalValueForMapping(), is(equalTo("b")));
        assertThat(result.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.number2.internalValueForMapping(), is(equalTo("2")));
    }

    @Test
    public void givenComplexTypeWithArray_whenDeserializing_thenReturnObject() {
        final String given = "{\"array\":[\"1\", \"2\", \"3\"]}";
        final AComplexTypeWithArray result = theDefaultDeserializer().deserializeJson(given, AComplexTypeWithArray.class);
        assertThat(result, is(notNullValue()));
        assertThat(result.array, is(notNullValue()));
        assertThat(result.array.length, is(equalTo(3)));
        assertThat(Arrays.asList(result.array), hasItem(ANumber.fromString("1")));
        assertThat(Arrays.asList(result.array), hasItem(ANumber.fromString("2")));
        assertThat(Arrays.asList(result.array), hasItem(ANumber.fromString("3")));
    }

    @Test
    public void givenComplexTypeWithInvalidArray_whenDeserializing_thenThrowCorrectException() {
        final String given = "{\"array\":[\"1\", \"51\", \"53\"]}";
        try {
            theDefaultDeserializer().deserializeJson(given, AComplexTypeWithArray.class);
            fail("should throw exception");
        } catch (final AggregatedValidationException e) {
            assertThat(e.getValidationErrors(), is(notNullValue()));
            assertThat(e.getValidationErrors().size(), is(equalTo(2)));
        }
    }

    @Test
    public void givenComplexNestedTypeJson_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                "}";
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class);
        assertThat(result, is(not(equalTo(null))));
        assertThat(result.complexType1, is(not(equalTo(null))));
        assertThat(result.complexType2, is(not(equalTo(null))));
        assertThat(result.complexType1.number1, is(equalTo(fromInt(1))));
        assertThat(result.complexType1.number2, is(equalTo(fromInt(2))));
        assertThat(result.complexType1.stringA, is(equalTo(fromString("a"))));
        assertThat(result.complexType1.stringB, is(equalTo(fromString("b"))));
        assertThat(result.complexType2.number1, is(equalTo(fromInt(3))));
        assertThat(result.complexType2.number2, is(equalTo(fromInt(4))));
        assertThat(result.complexType2.stringA, is(equalTo(fromString("c"))));
        assertThat(result.complexType2.stringB, is(equalTo(fromString("d"))));
    }

    @Test
    public void givenNull_whenDeserializing_thenThrowsError() {
        final String given = null;
        final String expectedMessage = "originalInput must not be null";
        try {
            theDefaultDeserializer().deserializeJson(given, AComplexType.class);
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenEmpty_whenDeserializing_thenReturnsNull() {
        final String given = "";
        final AComplexType result = theDefaultDeserializer().deserializeJson(given, AComplexType.class);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void givenInvalidJson_whenDeserializing_thenThrowsError() {
        final String given = "{\"number1\";\"1\",\"number2\":\"2\",\"stringA\"=\"a\",\"stringB\":\"b\"}";
        final String expectedMessage = "com.google.gson.stream.MalformedJsonException: Expected ':' " +
                "at line 1 column 12 path $.";
        try {
            theDefaultDeserializer().deserializeJson(given, AComplexType.class);
            fail("should throw JsonSyntaxException");
        } catch (final JsonSyntaxException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenIncompleteJson_whenDeserializing_thenFillsWithNull() {
        final String given = "{\"number1\":\"1\",\"stringA\":\"a\"}";
        final AComplexType result = theDefaultDeserializer().deserializeJson(given, AComplexType.class);
        assertThat(result.stringA, is(notNullValue()));
        assertThat(result.stringB, is(nullValue()));
        assertThat(result.number1, is(notNullValue()));
        assertThat(result.number2, is(nullValue()));
    }

    @Test
    public void givenJsonWithValidValues_whenDeserializing_thenReturnsObject() {
        final String given = "{\"number1\":\"21\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final AComplexTypeWithValidations result = theDefaultDeserializer().deserializeJson(given, AComplexTypeWithValidations.class);
        assertThat(result, is(notNullValue()));
        assertThat(result.number1, is(equalTo(fromInt(21))));
        assertThat(result.stringA, is(equalTo(fromString("a"))));

    }

    @Test
    public void givenJsonWithNestedValidationExceptions_whenDeserializing_thenReturnsOnlyOneValidationException() {
        final String given = "{\"node\": {\"leaf\":\"1234\"}}";
        try {
            final AComplexNestedValidatedType intermediate =
                    theDefaultDeserializer().deserializeJson(given, AComplexNestedValidatedType.class);
            fail("should throw validation exception");
        } catch (final AggregatedValidationException result) {
            assertThat(result.getValidationErrors().size(), is(equalTo(1)));
        }
    }

    @Test
    public void givenJson_whenDeserializingUsingAdapter_thenReturnsObject() {
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .withDataTransferObject(AComplexTypeWithCollections.class)
                .deserializedUsing(targetType -> new DeserializationDTOMethod() {
                    @Override
                    public Object deserialize(final Class<?> targetType,
                                              final Map<String, Object> elements) {
                        final ArrayList<AString> arrayList = new ArrayList<>();
                        arrayList.add(fromString("a"));
                        arrayList.add(fromString("b"));
                        final ANumber[] array = new ANumber[]{fromInt(1), fromInt(2)};
                        return AComplexTypeWithCollections.aComplexTypeWithCollection(arrayList, array);
                    }

                    @Override
                    public Map<String, Class<?>> elements(final Class<?> targetType) {
                        return new HashMap<>();
                    }
                })
                .build();
        final String given = "{}";
        final AComplexTypeWithCollections result = deserializer.deserializeJson(given, AComplexTypeWithCollections.class);
        assertThat(result.arrayList, hasItem(fromString("a")));
        assertThat(result.arrayList, hasItem(fromString("b")));
        assertThat(result.array.length, is(equalTo(2)));
    }

    @Test
    public void givenJson_whenDeserializingUsingDeserializingDTOMethod_thenUsesCustomMethod() {
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .excluding(AComplexTypeWithMap.class)
                .thatAre().deserializedUsing(targetType -> new DeserializationDTOMethod() {
                    @Override
                    public Object deserialize(final Class<?> targetType, final Map<String, Object> elements) throws Exception {
                        return aComplexType(
                                (AString) elements.get("stringA"),
                                (AString) elements.get("stringB"),
                                (ANumber) elements.get("number1"),
                                (ANumber) elements.get("number2"));
                    }

                    @Override
                    public Map<String, Class<?>> elements(final Class<?> targetType) {
                        final Map<String, Class<?>> elementsMap = new HashMap<>();
                        elementsMap.put("number1", ANumber.class);
                        elementsMap.put("number2", ANumber.class);
                        elementsMap.put("stringA", AString.class);
                        elementsMap.put("stringB", AString.class);
                        return elementsMap;
                    }
                })
                .mappingExceptionUsing(AValidationException.class, (t, p) -> {
                    final AValidationException e = (AValidationException) t;
                    return new ValidationError(e.getMessage(), e.getBlamedField());
                })
                .build();
        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final AComplexType result = deserializer.deserializeJson(given, AComplexType.class);
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void givenJson_whenDeserializingUsingDeserializingCPMethod_thenUsesCustomMethod() {
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsing(new DeserializationCPMethod() {
                    @Override
                    public void verifyCompatibility(final Class<?> targetType) {
                        return;
                    }

                    @Override
                    public Object deserialize(final String input, final Class<?> targetType) throws Exception {
                        return null;
                    }
                })
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(
                        and(
                                allClassesThatHaveAStaticFactoryMethodWithNonStringArguments(),
                                allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed(
                                        "internalValueForMapping"
                                )),
                                excluding(AComplexTypeWithMap.class, AComplexTypeWithCollections.class)
                        ))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(AValidationException.class, (t, p) -> {
                    final AValidationException e = (AValidationException) t;
                    return new ValidationError(e.getMessage(), e.getBlamedField());
                })
                .build();

        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final AComplexType result = deserializer.deserializeJson(given, AComplexType.class);
        assertThat(result, is(notNullValue()));
        assertThat(result.stringA, is(nullValue()));
        assertThat(result.stringB, is(nullValue()));
        assertThat(result.number1, is(nullValue()));
        assertThat(result.number2, is(nullValue()));
    }

    @Test
    public void givenCustomProviderForcustomPrimitive_whenDeserializing_returnsProvidedInstance() {
        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .withDataTransferObject(AComplexType.class)
                .deserializedUsingTheSingleFactoryMethod()
                .withCustomPrimitive(ANumber.class)
                .deserializedUsingTheMethodNamed("fromString")
                .withCustomPrimitive(AString.class)
                .deserializedUsingTheStaticMethod(DeserializerTest::aStaticProviderMethod)
                .build();
        final AComplexType result = deserializer.deserializeJson(given, AComplexType.class);
        assertThat(result.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.stringB.internalValueForMapping(), is(equalTo("test")));
    }

    @Test
    public void givenCustomProviderWithValidationExceptionForCustomPrimitive_whenDeserializing_returnsValidationException() {
        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .withDataTransferObject(AComplexType.class)
                .deserializedUsingTheSingleFactoryMethod()
                .withCustomPrimitive(ANumber.class)
                .deserializedUsingTheMethodNamed("fromString")
                .withCustomPrimitive(AString.class)
                .deserializedUsingTheStaticMethod(input -> {
                    throw anException("an exception");
                })
                .mappingExceptionUsing(AnException.class, (e, path) -> new ValidationError(e.getMessage(), path))
                .build();
        try {
            deserializer.deserializeJson(given, AComplexType.class);
            fail("should throw an exception");
        } catch (final AggregatedValidationException result) {
            assertThat(result.getValidationErrors().size(), is(equalTo(2)));
        }
    }

    @Test
    public void givenComplexTypeJsonWithCustomCPMethod_whenDeserializing_thenReturnAComplexObject() {
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .excluding(AString.class)
                .excluding(AnException.class)
                .thatAre().deserializedUsingTheMethodNamed("fromString")
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(
                        and(
                                allClassesThatHaveAStaticFactoryMethodWithNonStringArguments(),
                                allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed(
                                        "internalValueForMapping"
                                )),
                                excluding(AComplexTypeWithMap.class, AComplexTypeWithCollections.class)
                        )
                )
                .excluding(AComplexTypeWithMap.class)
                .excluding(AComplexTypeWithCollections.class)
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(AValidationException.class, (t, p) -> {
                    final AValidationException e = (AValidationException) t;
                    return new ValidationError(e.getMessage(), e.getBlamedField());
                })
                .withCustomPrimitive(AString.class)
                .deserializedUsing(new DeserializationCPMethod() {
                    @Override
                    public void verifyCompatibility(final Class<?> targetType) {
                        return;
                    }

                    @Override
                    public Object deserialize(final String input, final Class<?> targetType) throws Exception {
                        return fromString("test");
                    }
                })
                .build();
        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final AComplexType result = deserializer.deserializeJson(given, AComplexType.class);
        assertThat(result.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.number2.internalValueForMapping(), is(equalTo("2")));
    }

    @Test
    public void givenComplexTypeJsonWithCustomDTOMethod_whenDeserializing_thenReturnAComplexObject() {
        final Deserializer deserializer = aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives().filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .excluding(AnException.class)
                .thatAre().deserializedUsingTheMethodNamed("fromString")
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(
                        and(
                                allClassesThatHaveAStaticFactoryMethodWithNonStringArguments(),
                                allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed(
                                        "internalValueForMapping"
                                )),
                                excluding(AComplexTypeWithMap.class, AComplexTypeWithCollections.class)
                        )
                )
                .excluding(AComplexType.class)
                .excluding(AComplexTypeWithMap.class)
                .excluding(AComplexTypeWithCollections.class)
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(AValidationException.class, (t, p) -> {
                    final AValidationException e = (AValidationException) t;
                    return new ValidationError(e.getMessage(), e.getBlamedField());
                })
                .withDataTransferObject(AComplexType.class)
                .deserializedUsing(targetType -> new DeserializationDTOMethod() {
                    @Override
                    public Object deserialize(final Class<?> targetType, final Map<String, Object> elements) throws Exception {
                        return AComplexType.aComplexType(
                                fromString("test"),
                                fromString("test"),
                                ANumber.fromInt(1),
                                ANumber.fromInt(2));
                    }

                    @Override
                    public Map<String, Class<?>> elements(final Class<?> targetType) {
                        return new HashMap<>();
                    }
                })
                .build();
        final String given = "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}";
        final AComplexType result = deserializer.deserializeJson(given, AComplexType.class);
        assertThat(result.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.number2.internalValueForMapping(), is(equalTo("2")));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingPropertyNameAndStringValue_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                "}";
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class, injector -> injector
                .put("complexType1.stringB", "test")
                .put("complexType2.number1", "45"));
        assertThat(result.complexType1.stringA.internalValueForMapping(), is(equalTo("a")));
        assertThat(result.complexType1.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.complexType1.number2.internalValueForMapping(), is(equalTo("2")));
        assertThat(result.complexType2.stringA.internalValueForMapping(), is(equalTo("c")));
        assertThat(result.complexType2.stringB.internalValueForMapping(), is(equalTo("d")));
        assertThat(result.complexType2.number1.internalValueForMapping(), is(equalTo("45")));
        assertThat(result.complexType2.number2.internalValueForMapping(), is(equalTo("4")));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingPropertyNameAndInstance_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                "}";
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class, injector -> injector
                .put("complexType1.stringB", fromString("test"))
                .put("complexType2.number1", ANumber.fromString("45")));
        assertThat(result.complexType1.stringA.internalValueForMapping(), is(equalTo("a")));
        assertThat(result.complexType1.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.complexType1.number2.internalValueForMapping(), is(equalTo("2")));
        assertThat(result.complexType2.stringA.internalValueForMapping(), is(equalTo("c")));
        assertThat(result.complexType2.stringB.internalValueForMapping(), is(equalTo("d")));
        assertThat(result.complexType2.number1.internalValueForMapping(), is(equalTo("45")));
        assertThat(result.complexType2.number2.internalValueForMapping(), is(equalTo("4")));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingInstanceAndType_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                "}";
        final Serializable givenInterface = fromString("test");
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class, injector -> injector
                .put(AString.class, givenInterface));
        assertThat(result.complexType1.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.complexType1.number2.internalValueForMapping(), is(equalTo("2")));
        assertThat(result.complexType2.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.number1.internalValueForMapping(), is(equalTo("3")));
        assertThat(result.complexType2.number2.internalValueForMapping(), is(equalTo("4")));
    }

    @Test
    public void givenComplexTypeJsonWithInjectorUsingInstance_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}" +
                "}";
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class, injector -> injector
                .put(fromString("test"))
                .put(fromString("test")));
        assertThat(result.complexType1.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.complexType1.number2.internalValueForMapping(), is(equalTo("2")));
        assertThat(result.complexType2.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.number1.internalValueForMapping(), is(equalTo("3")));
        assertThat(result.complexType2.number2.internalValueForMapping(), is(equalTo("4")));
    }

    @Test
    public void givenComplexTypeWithIncompleteJsonWithInjectorUsingInstance_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\"}" +
                "}";
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class, injector -> injector
                .put(fromString("test")));
        assertThat(result.complexType1.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType1.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.complexType1.number2.internalValueForMapping(), is(equalTo("2")));
        assertThat(result.complexType2.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.stringB.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.number1.internalValueForMapping(), is(equalTo("3")));
        assertThat(result.complexType2.number2.internalValueForMapping(), is(equalTo("4")));
    }

    @Test
    public void givenComplexTypeWithIncompleteJsonWithInjectorUsingPropertyPath_whenDeserializing_thenReturnAComplexObject() {
        final String given = "{" +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\"}" +
                "}";
        final AComplexNestedType result = theDefaultDeserializer().deserializeJson(given, AComplexNestedType.class, injector -> injector
                .put("complexType2.stringA", fromString("test")));
        assertThat(result.complexType1.stringA.internalValueForMapping(), is(equalTo("a")));
        assertThat(result.complexType1.stringB.internalValueForMapping(), is(equalTo("b")));
        assertThat(result.complexType1.number1.internalValueForMapping(), is(equalTo("1")));
        assertThat(result.complexType1.number2.internalValueForMapping(), is(equalTo("2")));
        assertThat(result.complexType2.stringA.internalValueForMapping(), is(equalTo("test")));
        assertThat(result.complexType2.stringB, is(nullValue()));
        assertThat(result.complexType2.number1.internalValueForMapping(), is(equalTo("3")));
        assertThat(result.complexType2.number2.internalValueForMapping(), is(equalTo("4")));
    }
}
