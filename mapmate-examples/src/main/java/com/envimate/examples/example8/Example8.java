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

package com.envimate.examples.example8;


import com.envimate.examples.example8.domain.AccountId;
import com.envimate.examples.example8.domain.User;
import com.envimate.examples.example8.domain.UserName;
import com.envimate.examples.example8.events.EventBus;
import com.envimate.examples.example8.events.SimpleEventBus;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Example8 {

    /*
        Transient fields are not serialized.
    */
    public static final void serializingWithTransientFields() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example8.domain")
                .forCustomPrimitives()
                .filteredBy(
                        allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example8.domain")
                .forDataTransferObjects()
                .filteredBy(
                        allBut(
                                allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                .thatAre().serializedByItsPublicFields()
                .build();

        final User user = User.user(
                SimpleEventBus.simpleEventBus("example8"),
                AccountId.fromString("123456"),
                UserName.fromString("john_doe"),
                null);

        final String json = serializer.serialize(user);
        System.out.println(json);

//        Output:
//        {
//            "accountId": "123456",
//            "userName": "john_doe"
//        }
    }

    /*
        Injecting transient fields is still possible. Allowing you to gain full control of i.e. injected singletons or services.
        Also note that EventBus is an interface; meaning you can implement interfaces using this method.
    */
    public static final void deserializeWithInjectedTransientInterfaces() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example8.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example8.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();

        final String json = "" +
                "{" +
                "    \"accountId\": \"123456\"," +
                "    \"userName\": \"john_doe\"," +
                "    \"address\": {" +
                "       \"street\": \"Downingstreet\"," +
                "       \"city\": \"London\"" +
                "   }" +
                "}";

        final User user = deserializer.deserialize(json, User.class, injector -> injector
                .put(EventBus.class, SimpleEventBus.simpleEventBus("exampleBLA"))
                .put("address.eventBus", EventBus.class, SimpleEventBus.simpleEventBus("example8")));

        System.out.println(user);

    }

}
