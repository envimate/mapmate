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

package com.envimate.mapmate.examples;

import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

import static com.envimate.mapmate.filters.ClassFilters.includingAll;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

class SerializerProvider implements Provider<Serializer> {

    @Inject
    SerializerProvider() {
    }

    @Override
    public Serializer get() {
        return aSerializer()
                .withJsonMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.mapmate.examples.domain")
                .forCustomPrimitives()
                .filteredBy(includingAll())
                .thatAre().serializedUsingTheMethodNamed("internalValueForMapping")
                .thatScansThePackage("com.envimate.mapmate.examples.domain")
                .forDataTransferObjects()
                .filteredBy(includingAll())
                .thatAre().serializedByItsPublicFields()
                .build();
    }
}
