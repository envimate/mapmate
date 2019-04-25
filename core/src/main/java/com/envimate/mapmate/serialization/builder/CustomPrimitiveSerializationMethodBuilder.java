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

import com.envimate.mapmate.reflections.MethodName;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;

import java.util.function.Function;

import static com.envimate.mapmate.reflections.MethodName.fromString;
import static com.envimate.mapmate.serialization.methods.NamedMethodSerializationCPMethod.theNamedMethodSerializationCPMethod;
import static com.envimate.mapmate.serialization.methods.ProvidedMethodSerializationCPMethod.providedMethodSerializationCPMethod;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@SuppressWarnings("unused")
public final class CustomPrimitiveSerializationMethodBuilder<T> {
    private final Class<T> type;
    private final Function<SerializationCPMethod, SerializerBuilder> resultConsumer;

    private CustomPrimitiveSerializationMethodBuilder(
            final Class<T> type, final Function<SerializationCPMethod, SerializerBuilder> resultConsumer) {
        this.type = type;
        this.resultConsumer = resultConsumer;
    }

    static <T> CustomPrimitiveSerializationMethodBuilder<T> aCustomPrimitiveSerializationMethodBuilder(
            final Class<T> type, final Function<SerializationCPMethod, SerializerBuilder> resultConsumer) {
        return new CustomPrimitiveSerializationMethodBuilder<>(type, resultConsumer);
    }

    public SerializerBuilder serializedUsingTheMethodNamed(final String methodName) {
        final MethodName methodNameObject = fromString(methodName);
        return serializedUsing(theNamedMethodSerializationCPMethod(methodNameObject));
    }

    public SerializerBuilder serializedUsingTheMethod(final Function<T, String> method) {
        return serializedUsing(providedMethodSerializationCPMethod(this.type, method));
    }

    public SerializerBuilder serializedUsing(final SerializationCPMethod method) {
        validateNotNull(method, "method");
        return this.resultConsumer.apply(method);
    }
}
