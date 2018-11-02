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

package com.envimate.examples.example6;

import com.envimate.examples.example1.domain.auth.UserAuth;
import com.envimate.examples.example6.domain.auth.UserAuthDTO;
import com.envimate.examples.example6.domain.register.HouseNrAddition;
import com.envimate.examples.example6.domain.user.*;
import com.envimate.examples.utils.Utils;
import com.envimate.mapmate.DefinitionNotFoundException;
import com.envimate.mapmate.serialization.CircularReferenceException;
import com.envimate.mapmate.serialization.Serializer;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;
import com.google.gson.Gson;

import java.util.function.Function;

import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Example6 {

    /*
        Package scanning to retrieve valid domain structures can be done in various ways and is driven by class filters.
        Mapmate comes with some class filters out of the box, but you can write them yourself too!

        In this example we've fabricated an annotation to filter.
     */
    public static final void packageScanningClassFiltersCustom() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example6.domain")
                .forCustomPrimitives()
                .filteredBy(
                        allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example6.domain")
                .forDataTransferObjects()
                .filteredBy(type -> type.getAnnotation(DTO.class) != null)
                .thatAre().serializedByItsPublicFields()
                .build();

        System.out.println(serializer.getDefinitions());
    }

    /*
        Another example class filter DTO's by class suffix. This one comes out of the box.
     */
    public static final void packageScanningClassFiltersSuffix() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example6.domain")
                    .forCustomPrimitives()
                    .filteredBy(
                            allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                    .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example6.domain")
                    .forDataTransferObjects()
                    .identifiedByClassNameSuffix("DTO")
                    .thatAre().serializedByItsPublicFields()
                .build();

        System.out.println(serializer.getDefinitions());
    }

    /*
        It's also possible to exclude classes and/or add single classes.
     */
    public static final void packageScanningExclusionAndAdditions() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example6.domain")
                    .forCustomPrimitives()
                    .filteredBy(
                            allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                    .excluding(HouseNrAddition.class)
                    .thatAre().serializedUsingTheMethodNamed("getValue")
                .withCustomPrimitive(HouseNrAddition.class)
                    .serializedUsingTheMethodNamed("getValue")
                .withDataTransferObject(UserAuthDTO.class)
                    .serializedByItsPublicFields()
                .withDataTransferObject(UserAuthDTO.class)
                    .serializedByItsPublicFields()
                .build();

        System.out.println(serializer.getDefinitions());
    }

    /*
        Some safety features are built in, making sure the domain is air-tight.
        In this example we're try to serialize a User.class which contains reference to itself.
     */
    public static final void packageScanningCircularReference() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example6.domain")
                    .forCustomPrimitives()
                    .filteredBy(
                            allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                    .thatAre().serializedUsingTheMethodNamed("getValue")
                .withDataTransferObject(User.class)
                    .serializedByItsPublicFields()
                .build();

        final User user = User.user(
                UserName.fromString("john_doe90"),
                UserEmail.fromString("john_doe@mydomain.com"),
                null
        );
        user.ownedBy = user;

        try {
            serializer.serialize(user);
        } catch(final CircularReferenceException e) {
            System.out.println(e.getMessage());
        }
    }

    /*
        Another safety feature will defend against unknown definitions.
        This example shows us serializing a DTO with an unknown custom primitive, by simply excluding the username.
     */
    public static final void packageScanningUnknownReferenceType() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example6.domain")
                    .forCustomPrimitives()
                    .filteredBy(
                            allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                    .excluding(AccountId.class)
                    .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example6.domain")
                    .forDataTransferObjects()
                    .filteredBy(
                            allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                    .thatAre().serializedByItsPublicFields()
                .build();

        final UserAuthDTO userAuth = UserAuthDTO.userAuth(
                AccountId.fromString("098431"),
                UserName.fromString("john_doe90"),
                Password.fromString("mysecret123")
        );

        try{
            serializer.serialize(userAuth);
        } catch (final DefinitionNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    /*
        Thus far we've only used serializedUsingTheMethodNamed as a serialization method. This example shows all other methods.
        Similar features are available for deserialization!
     */
    public static final void packageScanningSerializingMethods() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example6.domain")
                      .forCustomPrimitives()
                      .filteredBy(
                            allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                      .thatAre().serializedUsingTheMethodNamed("getValue")
//                    .thatAre().serializedUsingTheMethod(Utils::genericSerializer)
//                    .thatAre().serializedUsing(new SerializationCPMethod() {
//                        @Override
//                        public void verifyCompatibility(final Class<?> targetType) {
//                            // Opportunity to deny this for certain types.
//                        }
//
//                        @Override
//                        public String serialize(final Object o) {
//                            return o.toString();
//                        }
//                     })
                .thatScansThePackage("com.envimate.examples.example6.domain")
                      .forDataTransferObjects()
                      .filteredBy(
                            allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                      .thatAre().serializedByItsPublicFields()
//                    .thatAre().serializedUsing((object, serializerCallback) -> object.toString())
                .withCustomPrimitive(AccountId.class)
                      .serializedUsingTheMethodNamed("getValue")
//                    .serializedUsingTheMethod(Utils::genericSerializer)
//                    .serializedUsing(new SerializationCPMethod() {
//                        @Override
//                        public void verifyCompatibility(final Class<?> targetType) {
//                            // Opportunity to deny this for certain types.
//                        }
//
//                        @Override
//                        public String serialize(final Object object) {
//                            return object.toString();
//                        }
//                    })
                .withDataTransferObject(UserAuth.class)
                      .serializedByItsPublicFields()
//                    .serializedUsing((o, serializerCallback) -> o.toString())
                .build();
    }
}
