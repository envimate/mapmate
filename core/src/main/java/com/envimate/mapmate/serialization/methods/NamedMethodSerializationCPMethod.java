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

package com.envimate.mapmate.serialization.methods;

import com.envimate.mapmate.CustomPrimitiveSerializationMethodCallException;
import com.envimate.mapmate.reflections.MethodName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;

import static com.envimate.mapmate.reflections.Reflections.findPublicStringMethodByName;
import static com.envimate.mapmate.serialization.methods.SerializationMethodNotCompatibleException.serializationMethodNotCompatibleException;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public final class NamedMethodSerializationCPMethod implements SerializationCPMethod {

    private final MethodName methodName;

    private NamedMethodSerializationCPMethod(final MethodName methodName) {
        this.methodName = methodName;
    }

    public static SerializationCPMethod theNamedMethodSerializationCPMethod(final MethodName methodName) {
        return new NamedMethodSerializationCPMethod(methodName);
    }

    @Override
    public void verifyCompatibility(final Class<?> targetType) {
        try {
            final Method method = targetType.getMethod(this.methodName.internalValueForMapping());
            if (method.getParameterCount() != 0) {
                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "' " +
                        "does not have a zero argument String method" +
                        " named '" + this.methodName.internalValueForMapping() + "'");
            }
            if (method.getReturnType() != String.class) {
                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "' " +
                        "does not have a zero argument String method" +
                        " named '" + this.methodName.internalValueForMapping() + "'");
            }
            if (!isPublic(method.getModifiers())) {
                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "'" +
                        " does not have a zero argument String method" +
                        " named '" + this.methodName.internalValueForMapping() + "'");
            }
            if (isStatic(method.getModifiers())) {
                throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "'" +
                        " does not have a zero argument String method" +
                        " named '" + this.methodName.internalValueForMapping() + "'");
            }
        } catch (final NoSuchMethodException e) {
            throw serializationMethodNotCompatibleException("class '" + targetType.getName() + "'" +
                    " does not have a zero argument String method" +
                    " named '" + this.methodName.internalValueForMapping() + "'", e);
        }
    }

    @Override
    public String serialize(final Object object) {
        final Class<?> type = object.getClass();
        final MethodHandle methodHandle = findPublicStringMethodByName(type, this.methodName.internalValueForMapping());
        try {
            return (String) methodHandle.invoke(object);
        } catch (final WrongMethodTypeException | ClassCastException e) {
            throw CustomPrimitiveSerializationMethodCallException.fromThrowable(
                    "this should never happen, contact the developers.",
                    e,
                    type,
                    methodHandle,
                    object);
        } catch (final Throwable throwable) {
            throw CustomPrimitiveSerializationMethodCallException.fromThrowable(
                    "exception invoking serialization method",
                    throwable,
                    type,
                    methodHandle,
                    object);
        }
    }
}
