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

import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;

import java.util.function.Function;

import static com.envimate.mapmate.deserialization.methods.NamedFactoryMethodCPMethod.theNamedFactoryMethodCPMethod;
import static com.envimate.mapmate.deserialization.methods.SingleFactoryMethodCPDeserializationMethod.theSingleFactoryMethodCPDeserializationMethod;
import static com.envimate.mapmate.deserialization.methods.StaticMethodCPMethod.theStaticMethodCPMethod;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static com.envimate.mapmate.validators.RequiredStringValidator.validateNotNullNorEmpty;

public final class CustomPrimitiveDeserializationMethodBuilder {

    private final Function<DeserializationCPMethod, DeserializerBuilder> resultConsumer;

    private CustomPrimitiveDeserializationMethodBuilder(final Function<DeserializationCPMethod,
            DeserializerBuilder> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    static CustomPrimitiveDeserializationMethodBuilder aCustomPrimitiveDeserializationMethodBuilder(
            final Function<DeserializationCPMethod, DeserializerBuilder> resultConsumer) {
        return new CustomPrimitiveDeserializationMethodBuilder(resultConsumer);
    }

    public DeserializerBuilder deserializedUsingTheStaticMethodWithSingleStringArgument() {
        return deserializedUsing(theSingleFactoryMethodCPDeserializationMethod());
    }

    public DeserializerBuilder deserializedUsingTheMethodNamed(final String methodName) {
        validateNotNullNorEmpty(methodName, "methodName");
        return deserializedUsing(theNamedFactoryMethodCPMethod(methodName));
    }

    public DeserializerBuilder deserializedUsingTheStaticMethod(final Function<String, ?> function) {
        validateNotNull(function, "function");
        return deserializedUsing(theStaticMethodCPMethod(function));
    }

    public DeserializerBuilder deserializedUsing(final DeserializationCPMethod method) {
        validateNotNull(method, "method");
        return this.resultConsumer.apply(method);
    }
}
