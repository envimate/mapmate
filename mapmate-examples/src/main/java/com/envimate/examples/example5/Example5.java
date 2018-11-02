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

package com.envimate.examples.example5;

import com.envimate.examples.example5.domain.auth.AccountId;
import com.envimate.examples.example5.domain.auth.Password;
import com.envimate.examples.example5.domain.auth.UserAuth;
import com.envimate.examples.example5.domain.auth.UserName;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.Unmarshaller;
import com.envimate.mapmate.serialization.Marshaller;
import com.envimate.mapmate.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Example5 {

    /*
        Serializing using an YAML dataformat
     */
    public static final void yamlSerialization() {
        final Serializer serializer = aSerializer()
                .withMarshaller(o -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                        return mapper.writeValueAsString(o);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .thatScansThePackage("com.envimate.examples.example5.domain")
                .forCustomPrimitives()
                .filteredBy(
                        allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example5.domain")
                .forDataTransferObjects()
                .filteredBy(
                        allBut(
                                allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                .thatAre().serializedByItsPublicFields()
                .build();

        final UserAuth userAuth = UserAuth.userAuth(
                AccountId.fromString("098431"),
                UserName.fromString("john_doe90"),
                Password.fromString("mysecret123")
        );

        final String json = serializer.serialize(userAuth);
        System.out.println(json);

//        Output:
//        ---
//        userName: "john_doe90"
//        accountId: "098431"
//        password: "mysecret123"
    }

    /*
        Deserializing using an YAML dataformat
     */
    public static final void yamlDeserialization() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Unmarshaller() {
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        try {
                            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                            return mapper.readValue(input, type);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .thatScansThePackage("com.envimate.examples.example5.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example5.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();

        final String json = "" +
                "---\n" +
                "userName: \"john_doe90\"\n" +
                "accountId: \"098431\"\n" +
                "password: \"mysecret123\"";

        final UserAuth userAuth = deserializer.deserialize(json, UserAuth.class);
        System.out.println(userAuth);
    }

}
