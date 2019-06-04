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

package com.envimate.mapmate.deserialization.builder;

import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethodFactory;

import java.util.function.Function;

import static com.envimate.mapmate.deserialization.methods.NamedFactoryMethodDTODeserializationMethod.namedFactoryMethodDTODeserializationMethod;
import static com.envimate.mapmate.deserialization.methods.SingleFactoryMethodDTODeserializationMethod.singleFactoryMethodDTODeserializationDTOMethod;
import static com.envimate.mapmate.deserialization.methods.TheRightFactoryMethodDTODeserializationMethod.theRightFactoryMethodDTODeserializationDTOMethod;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public final class DataTransferObjectDeserializationMethodBuilder {

    private final Function<DeserializationDTOMethodFactory, DeserializerBuilder> resultConsumer;

    private DataTransferObjectDeserializationMethodBuilder(final Function<DeserializationDTOMethodFactory,
            DeserializerBuilder> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    static DataTransferObjectDeserializationMethodBuilder aDataTransferObjectDeserializationMethodBuilder(
            final Function<DeserializationDTOMethodFactory, DeserializerBuilder> resultConsumer) {
        return new DataTransferObjectDeserializationMethodBuilder(resultConsumer);
    }

    public DeserializerBuilder deserializedUsingTheSingleFactoryMethod() {
        return deserializedUsing(singleFactoryMethodDTODeserializationDTOMethod());
    }

    public DeserializerBuilder deserializedUsingTheFactoryMethodNamed(final String methodName) {
        validateNotNull(methodName, "methodName");
        return deserializedUsing(namedFactoryMethodDTODeserializationMethod(methodName));
    }

    public DeserializerBuilder deserializedUsing(final DeserializationDTOMethodFactory methodFactory) {
        validateNotNull(methodFactory, "methodFactory");
        return this.resultConsumer.apply(methodFactory);
    }

    public DeserializerBuilder deserializedUsingTheFactoryMethodWithTheRightParameters() {
        return deserializedUsing(theRightFactoryMethodDTODeserializationDTOMethod());
    }
}
