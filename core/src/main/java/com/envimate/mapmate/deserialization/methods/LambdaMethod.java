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

package com.envimate.mapmate.deserialization.methods;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaMethod<T> implements DeserializationCPMethod {
    private final Function<String, T> deserializationMethod;

    public static <T> DeserializationCPMethod lambdaMethod(final Function<String, T> deserializationMethod) {
        return new LambdaMethod<T>(deserializationMethod);
    }

    @Override
    public void verifyCompatibility(final Class<?> targetType) {
//        try {
//            final Method method = targetType.getMethod(this.methodName.internalValueForMapping());
//            if (method.getParameterCount() != 0) {
//                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "' " +
//                        "does not have a zero argument String method" +
//                        " named '" + this.methodName.internalValueForMapping() + "'");
//            }
//            if (method.getReturnType() != String.class) {
//                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "' " +
//                        "does not have a zero argument String method" +
//                        " named '" + this.methodName.internalValueForMapping() + "'");
//            }
//            if (!isPublic(method.getModifiers())) {
//                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "'" +
//                        " does not have a zero argument String method" +
//                        " named '" + this.methodName.internalValueForMapping() + "'");
//            }
//            if (isStatic(method.getModifiers())) {
//                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "'" +
//                        " does not have a zero argument String method" +
//                        " named '" + this.methodName.internalValueForMapping() + "'");
//            }
//        } catch (final NoSuchMethodException e) {
//            throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "'" +
//                    " does not have a zero argument String method" +
//                    " named '" + this.methodName.internalValueForMapping() + "'", e);
//        }
    }

    @Override
    public T deserialize(final String input, final Class<?> targetType) throws Exception {
        return this.deserializationMethod.apply(input);
    }
}
