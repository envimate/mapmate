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

package com.envimate.mapmate.serialization;

import com.envimate.mapmate.DefinitionNotFoundException;
import com.envimate.mapmate.Lists;
import com.envimate.mapmate.domain.utils.AStrings;
import com.envimate.mapmate.domain.valid.*;
import com.envimate.mapmate.validators.CustomTypeValidationException;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.mapmate.Defaults.theDefaultSerializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;
import static com.envimate.mapmate.serialization.methods.ProvidedMethodSerializationCPMethod.providedMethodSerializationCPMethod;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class SerializerTest {

    @Test
    public void testMethodReferenceInCPSerializationMethod() {
        final Serializer serializer = aSerializer()
                .withJsonMarshaller(new Gson()::toJson)
                .withDataTransferObject(AComplexType.class)
                .serializedByItsPublicFields()
                .withCustomPrimitive(AString.class)
                .serializedUsingTheMethod(AString::internalValueForMapping)
                .withCustomPrimitive(ANumber.class)
                .serializedUsingTheMethod(ANumber::internalValueForMapping)
                .build();

        final AComplexType aComplexType = AComplexType.aComplexType(
                AString.fromString("asdf"),
                AString.fromString("qwer"),
                ANumber.fromInt(1),
                ANumber.fromInt(5555));
        final String result = serializer.serializeToJson(aComplexType);
        assertThat(result, is("{\"number1\":\"1\",\"number2\":\"5555\",\"stringA\":\"asdf\",\"stringB\":\"qwer\"}"));
    }

    @Test
    public void givenStringDomain_whenSerializing_thenReturnsJsonString() {
        final AString given = AString.fromString("test@test.test");
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("\"test@test.test\"")));
    }

    @Test
    public void givenNumberDomain_whenSerializing_thenReturnsJsonString() {
        final ANumber given = ANumber.fromInt(123);
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("\"123\"")));
    }

    @Test
    public void givenComplexDomain_whenSerializing_thenReturnsJsonString() {
        final AComplexType given = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}")));
    }

    @Test
    public void givenComplexDomainWithCollections_whenSerializing_thenReturnsJsonString() {
        final AComplexTypeWithCollections given = AComplexTypeWithCollections.aComplexTypeWithCollection(
                Lists.of(new AString[]{
                        AString.fromString("a"),
                        AString.fromString("b"),
                        AString.fromString("c")}),
                new ANumber[]{
                        ANumber.fromInt(1),
                        ANumber.fromInt(2),
                        ANumber.fromInt(3),
                });
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("{\"array\":[\"1\",\"2\",\"3\"],\"arrayList\":[\"a\",\"b\",\"c\"]}")));
    }

    @Test
    public void givenComplexDomainWithMap_whenSerializing_thenReturnsJsonString() {
        final Map<AString, ANumber> hashMap1 = new HashMap<>(2);
        hashMap1.put(AString.fromString("a"), ANumber.fromInt(1));
        hashMap1.put(AString.fromString("b"), ANumber.fromInt(2));
        final HashMap<AString, ANumber> hashMap2 = new HashMap<>(2);
        hashMap2.put(AString.fromString("c"), ANumber.fromInt(3));
        hashMap2.put(AString.fromString("d"), ANumber.fromInt(4));
        final AComplexTypeWithMap given = AComplexTypeWithMap.aComplexTypeWithMap(hashMap1, hashMap2, null);
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("{\"map\":{\"a\":\"1\",\"b\":\"2\"},\"hashMap\":{\"d\":\"4\",\"c\":\"3\"}}")));
    }

    @Test
    public void givenComplexDomainWithMapContainingComplexDomains_whenSerializing_thenReturnsJsonString() {
        final AComplexType aComplexType1 = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));
        final AComplexType aComplexType2 = AComplexType.aComplexType(
                AString.fromString("c"),
                AString.fromString("d"),
                ANumber.fromInt(3),
                ANumber.fromInt(4));
        final HashMap<AString, AComplexType> hashMap = new HashMap<>(2);
        hashMap.put(AString.fromString("a"), aComplexType1);
        hashMap.put(AString.fromString("b"), aComplexType2);
        final AComplexTypeWithMap given = AComplexTypeWithMap.aComplexTypeWithMap(null, null, hashMap);
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("{\"complexMap\":{" +
                "\"a\":{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}," +
                "\"b\":{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}}}")));
    }

    @Test
    public void givenComplexNestedDomain_whenSerializing_thenReturnsJsonString() {
        final AComplexNestedType given = AComplexNestedType.aComplexNestedType(
                AComplexType.aComplexType(
                        AString.fromString("a"),
                        AString.fromString("b"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(2)),
                AComplexType.aComplexType(
                        AString.fromString("c"),
                        AString.fromString("d"),
                        ANumber.fromInt(3),
                        ANumber.fromInt(4)));
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("{\"complexType2\":" +
                "{\"number1\":\"3\",\"number2\":\"4\",\"stringA\":\"c\",\"stringB\":\"d\"}," +
                "\"complexType1\":" +
                "{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"a\",\"stringB\":\"b\"}}")));
    }

    @Test
    public void givenNull_whenSerializing_thenThrowsError() {
        final String expectedMessage = "object must not be null";
        try {
            theDefaultSerializer().serializeToJson(null);
            fail("should throw NullPointerException");
        } catch (final CustomTypeValidationException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNonConfiguredComplexDomain_whenSerializing_thenThrowsError() {
        final ANonConfiguredDomain given = new ANonConfiguredDomain();
        final String expectedMessage = "no definition found for type 'com.envimate.mapmate.serialization.SerializerTest$ANonConfiguredDomain'";

        try {
            theDefaultSerializer().serializeToJson(given);
            fail("should throw UnsupportedOperationException");
        } catch (final DefinitionNotFoundException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenCyclicType_whenSerializing_thenThrowsError() {
        final ACyclicType given1 = ACyclicType.aCyclicType(AString.fromString("a"));
        final ACyclicType given2 = ACyclicType.aCyclicType(AString.fromString("b"));
        given1.aCyclicType = given2;
        given2.aCyclicType = given1;

        final String expectedMessage = "a circular reference has been detected for objects " +
                "of type com.envimate.mapmate.domain.valid.ACyclicType";

        try {
            theDefaultSerializer().serializeToJson(given1);
            fail("should throw CircularReferenceException");
        } catch (final CircularReferenceException result) {
            assertThat(result.getMessage(), is(equalTo(expectedMessage)));
        }
    }

    @Test
    public void givenNonCyclicType_whenSerializing_thenDoesNotThrowsError() {
        final ACyclicType given1 = ACyclicType.aCyclicType(AString.fromString("a"));
        final ACyclicType given2 = ACyclicType.aCyclicType(AString.fromString("b"));
        final ACyclicType given3 = ACyclicType.aCyclicType(AString.fromString("c"));

        given1.aCyclicType = given2;
        given2.aCyclicType = given3;

        theDefaultSerializer().serializeToJson(given1);
    }

    @Test
    public void givenComplexDomainWithNullValues_whenSerializing_thenExcludesFromJson() {
        final AComplexType given = AComplexType.aComplexType(
                AString.fromString("a"),
                null,
                ANumber.fromInt(1),
                null);
        final String result = theDefaultSerializer().serializeToJson(given);
        assertThat(result, is(equalTo("{\"number1\":\"1\",\"stringA\":\"a\"}")));
    }

    @Test
    public void givenComplexDomainUsingCustomDTOMethod_whenSerializing_thenReturnsJsonString() {
        final Serializer serializer = aSerializer()
                .withJsonMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(includingAll())
                .excluding(AComplexType.class)
                .thatAre().serializedByItsPublicFields()
                .withDataTransferObject(AComplexType.class)
                .serializedUsing((object, serializerCallback) -> "test")
                .build();
        final AComplexType given = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));
        final String result = serializer.serializeToJson(given);
        assertThat(result, is(equalTo("\"test\"")));
    }

    @Test
    public void givenComplexDomainUsingCustomCPMethod_whenSerializing_thenReturnsJsonString() {
        final Serializer serializer = aSerializer()
                .withJsonMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                .excluding(AString.class)
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping")))
                .thatAre().serializedByItsPublicFields()
                .withCustomPrimitive(AString.class)
                .serializedUsing(targetType -> providedMethodSerializationCPMethod(targetType, o -> "test"))
                .build();
        final AComplexType given = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));
        final String result = serializer.serializeToJson(given);
        assertThat(result, is(equalTo("{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"test\",\"stringB\":\"test\"}")));
    }

    @Test
    public void givenComplexDomainUsingCustomCPStaticMethod_whenSerializing_thenReturnsJsonString() {
        final Serializer serializer = aSerializer()
                .withJsonMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                .excluding(AString.class)
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping")))
                .thatAre().serializedByItsPublicFields()
                .withCustomPrimitive(AString.class)
                .serializedUsing(targetType -> providedMethodSerializationCPMethod(targetType, AStrings::provide))
                .build();
        final AComplexType given = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));
        final String result = serializer.serializeToJson(given);
        assertThat(result, is(equalTo("{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"provided\",\"stringB\":\"provided\"}")));
    }

    @Test
    public void givenComplexDomainUsingInjector_whenSerializing_thenReturnsJsonString() {
        final AComplexType given = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));
        final String result = theDefaultSerializer().serialize(given, json(), input -> {
            input.put("stringA", "test");
            return input;
        });
        assertThat(result, is(equalTo("{\"number1\":\"1\",\"number2\":\"2\",\"stringA\":\"test\",\"stringB\":\"b\"}")));
    }

    public static class ANonConfiguredDomain {

    }
}
