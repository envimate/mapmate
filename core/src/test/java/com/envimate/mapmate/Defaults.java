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

package com.envimate.mapmate;

import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.validation.ValidationError;
import com.envimate.mapmate.domain.valid.AComplexTypeWithCollections;
import com.envimate.mapmate.domain.valid.AComplexTypeWithMap;
import com.envimate.mapmate.domain.valid.AValidationException;
import com.envimate.mapmate.domain.valid.AnException;
import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Defaults {

    private Defaults() {
    }

    public static Serializer theDefaultSerializer() {
        return aSerializer()
                .withJsonMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping"))
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping")))
                .thatAre().serializedByItsPublicFields()
                .build();
    }

    @SuppressWarnings("CastToConcreteClass")
    public static Deserializer theDefaultDeserializer() {
        return aDeserializer()
                .withJsonUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.mapmate.domain.valid")
                .forDataTransferObjects()
                .filteredBy(and(
                        allClassesThatHaveAStaticFactoryMethodWithNonStringArguments(),
                        allBut(
                                allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("internalValueForMapping")
                        )
                ))
                .excluding(AComplexTypeWithMap.class)
                .excluding(AComplexTypeWithCollections.class)
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(AValidationException.class, (t, propertyPath) -> {
                    final AValidationException e = t;
                    return new ValidationError(e.getMessage(), e.getBlamedField());
                })
                .mappingExceptionUsing(AnException.class, (t, p) -> {
                    return new ValidationError(t.getMessage(), p);
                })
                .build();

    }
}
