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

package com.envimate.examples.example7;

import com.envimate.examples.example7.domain.auth.*;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;

import java.util.Map;
import java.util.function.Function;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Example7 {

    /*
        Injectors allow for injecting values from outside the input. In below example we try and serialize a change-password request.
        Only later we inject the authenticated user.
     */
    public static final void serializingUsingInjectors() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example7.domain")
                .forCustomPrimitives()
                .filteredBy(
                        allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example7.domain")
                .forDataTransferObjects()
                .filteredBy(
                        allBut(
                                allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                .thatAre().serializedByItsPublicFields()
                .build();

        final ChangePasswordRequest userAuth = ChangePasswordRequest.changePasswordRequest(
                null,
                Password.fromString("mysecret123"),
                Password.fromString("mysecret123")
        );

        final String json = serializer.serialize(userAuth, stringObjectMap -> {
            final User authenitcatedUser = User.user(
                    AccountId.fromString("12345"),
                    UserName.fromString("john_doe90")
            );
            stringObjectMap.put("authenticatedUser", authenitcatedUser);
            return stringObjectMap;
        });
        System.out.println(json);

//        Output:
//        {
//            "authenticatedUser":{
//              "userName": "john_doe90",
//              "accountId": "098431"
//            }
//            "newPassword": "mysecret123",
//            "newPassword": "mysecret123"
//        }
    }

    /*
        Similarly for deserialition.
     */
    public static final void deserializingUsingInjectors() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example7.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example7.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();

        final String json = "" +
                "{" +
                "    \"newPassword\": \"mysecret123\"," +
                "    \"newPasswordConfirm\": \"mysecret123\"" +
                "}";

        final ChangePasswordRequest request = deserializer.deserialize(json, ChangePasswordRequest.class, injector -> {
            final User authenitcatedUser = User.user(
                    AccountId.fromString("12345"),
                    UserName.fromString("john_doe90")
            );
            return injector.put("authenticatedUser", authenitcatedUser);
        });
        System.out.println(request);
    }

    /*
        A single field can know multiple viable injections, in which case the most specific first found is used.
        It also accepts string value's, which will be considered a json blob.
     */
    public static final void deserializingUsingInjectorsAdvanced() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example7.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example7.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();

        final String json = "" +
                "{" +
                "    \"newPassword\": \"mysecret123\"," +
                "    \"newPasswordConfirm\": \"mysecret123\"" +
                "}";

        final ChangePasswordRequest request = deserializer.deserialize(json, ChangePasswordRequest.class, injector -> injector
                .put("newPassword", Password.fromString("mysecret1"))
                .put("newPassword", Password.fromString("mysecret2"))
                .put(Password.fromString("oursecret"))
                .put("newPasswordConfirm", "some raw json value"));
        System.out.println(request);
    }
}
