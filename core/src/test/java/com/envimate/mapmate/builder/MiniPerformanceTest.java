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

package com.envimate.mapmate.builder;

import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.builder.DeserializerBuilder;
import com.envimate.mapmate.filters.ClassFilters;
import com.envimate.mapmate.serialization.Serializer;
import com.envimate.mapmate.serialization.builder.SerializerBuilder;
import com.google.gson.Gson;

public final class MiniPerformanceTest {
    final static Gson gson = new Gson();
    public static final String EMAIL_JSON = "{" +
            "\"receiver\":\"receiver@example.com\"," +
            "\"body\":\"Hello World!!!\"," +
            "\"sender\":\"sender@example.com\"," +
            "\"subject\":\"Hello\"" +
            "}";
    public static final String EMAIL_GSON_JSON = "{\"sender\":{\"value\":\"sender@example.com\"},\"receiver\":{\"value\":\"receiver@example.com\"},\"subject\":{\"value\":\"Hello\"},\"body\":{\"value\":\"Hello World!!!\"}}";

    public static final Email EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );
    public static final int COUNT = 1_000_000;


    public static MapMate theConventionalMapMateInstance() {


        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .usingJsonMarshallers(gson::toJson, gson::fromJson)
//                .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
                .build();
    }

    public static Serializer theOldSerializer() {
        return SerializerBuilder.aSerializerBuilder()
                .thatScansThePackage("com.envimate.mapmate.builder.models.conventional")
                .forCustomPrimitives()
                .filteredBy(ClassFilters.allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("stringValue"))
                .thatAre()
                .serializedUsingTheMethodNamed("stringValue")
                .withJsonMarshaller(gson::toJson)
                .thatScansThePackage("com.envimate.mapmate.builder.models.conventional")
                .forDataTransferObjects()
                .filteredBy(ClassFilters.allClassesThatHaveAStaticFactoryMethodWithNonStringArguments())
                .thatAre()
                .serializedByItsPublicFields()
                .withJsonMarshaller(gson::toJson)
                .build();
    }

    public static Deserializer theOldDeserializer() {
        final Gson gson = new Gson();

        return DeserializerBuilder.aDeserializerBuilder()
                .thatScansThePackage("com.envimate.mapmate.builder.models.conventional")
                .forCustomPrimitives()
                .filteredBy(ClassFilters.allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("stringValue"))
                .thatAre()
                .deserializedUsingTheMethodNamed("fromStringValue")
                .withJsonUnmarshaller(gson::fromJson)
                .thatScansThePackage("com.envimate.mapmate.builder.models.conventional")
                .forDataTransferObjects()
                .filteredBy(ClassFilters.allClassesThatHaveAStaticFactoryMethodWithNonStringArguments())
                .thatAre()
                .deserializedUsingTheFactoryMethodNamed("deserialize")
                .withJsonUnmarshaller(gson::fromJson)
                .build();
    }

    public static void main(final String[] args) {
        final Serializer serializer = theConventionalMapMateInstance().serializer();
        final Serializer oldSerializer = theOldSerializer();

        final long start = System.currentTimeMillis();

        for (int i = 0; i < COUNT; i++) {
            serializer.serializeToJson(EMAIL);
        }

        System.out.println("NEW SERIALIZE " + COUNT + " run took " + (System.currentTimeMillis() - start) + " ms");

        final long startOld = System.currentTimeMillis();

        for (int i = 0; i < COUNT; i++) {
            oldSerializer.serializeToJson(EMAIL);
        }

        System.out.println("OLD SERIALIZE " + COUNT + " run took " + (System.currentTimeMillis() - startOld) + " ms");


        final long startGsonSerialize = System.currentTimeMillis();

        for (int i = 0; i < COUNT; i++) {
            gson.toJson(EMAIL);
        }

        System.out.println("GSON SERIALIZE " + COUNT + " run took " + (System.currentTimeMillis() - startGsonSerialize) + " ms");


        final Deserializer deserializer = theConventionalMapMateInstance().deserializer();
        final Deserializer oldDeserializer = theOldDeserializer();

        final long startOldDeserialize = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            oldDeserializer.deserializeJson(EMAIL_JSON, Email.class);
        }


        System.out.println("OLD DESERIALIZE " + COUNT + " run took " + (System.currentTimeMillis() - startOldDeserialize) + " ms");

        final long startNewDeserializer = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            deserializer.deserializeJson(EMAIL_JSON, Email.class);
        }
        System.out.println("NEW DESERIALIZE " + COUNT + " run took " + (System.currentTimeMillis() - startNewDeserializer) + " ms");


        final long startGsonDeserialize = System.currentTimeMillis();

        for (int i = 0; i < COUNT; i++) {
            gson.fromJson(EMAIL_GSON_JSON, Email.class);
        }

        System.out.println("GSON DESERIALIZE " + COUNT + " run took " + (System.currentTimeMillis() - startGsonDeserialize) + " ms");


    }
}
