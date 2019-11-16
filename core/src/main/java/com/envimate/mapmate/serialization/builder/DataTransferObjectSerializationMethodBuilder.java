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

package com.envimate.mapmate.serialization.builder;

import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;

import java.util.function.Function;

import static com.envimate.mapmate.serialization.methods.PublicFieldsSerializationDTOMethod.thePublicFieldsSerializationDTOMethod;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public final class DataTransferObjectSerializationMethodBuilder {

    private final Function<SerializationDTOMethod, SerializerBuilder> resultConsumer;

    private DataTransferObjectSerializationMethodBuilder(final Function<SerializationDTOMethod,
            SerializerBuilder> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    static DataTransferObjectSerializationMethodBuilder aDataTransferObjectSerializationMethodBuilder(
            final Function<SerializationDTOMethod, SerializerBuilder> resultConsumer) {
        return new DataTransferObjectSerializationMethodBuilder(resultConsumer);
    }

    public SerializerBuilder serializedByItsPublicFields() {
        return serializedUsing(thePublicFieldsSerializationDTOMethod());
    }

    public SerializerBuilder serializedUsing(final SerializationDTOMethod method) {
        validateNotNull(method, "method");
        return this.resultConsumer.apply(method);
    }

}
