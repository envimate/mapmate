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

package com.envimate.mapmate.serialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;

import java.util.function.Function;

public final class SerializableDataTransferObject implements Definition {

    private final Class<?> type;
    private final SerializationDTOMethod serializationDTOMethod;

    private SerializableDataTransferObject(final Class<?> type, final SerializationDTOMethod serializationDTOMethod) {
        this.type = type;
        this.serializationDTOMethod = serializationDTOMethod;
    }

    public static SerializableDataTransferObject serializableDataTransferObject(
            final Class<?> type, final SerializationDTOMethod serializationDTOMethod) {
        return new SerializableDataTransferObject(type, serializationDTOMethod);
    }

    public Object serialize(final Object object, final Function<Object, Object> serializerCallback) {
        return this.serializationDTOMethod.serialize(object, serializerCallback);
    }

    @Override
    public boolean isCustomPrimitive() {
        return false;
    }

    @Override
    public boolean isDataTransferObject() {
        return true;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }
}
