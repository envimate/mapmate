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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;

import static com.envimate.mapmate.deserialization.DTOElements.theDTOElements;

public final class DeserializableDataTransferObject<T> implements Definition {

    private final Class<T> type;
    private final DeserializationDTOMethod deserializationMethod;

    private DeserializableDataTransferObject(final Class<T> type, final DeserializationDTOMethod deserializationMethod) {
        this.type = type;
        this.deserializationMethod = deserializationMethod;
    }

    public static <T> DeserializableDataTransferObject<T> deserializableDataTransferObject(
            final Class<T> type, final DeserializationDTOMethod deserializationMethod) {
        return new DeserializableDataTransferObject<>(type, deserializationMethod);
    }

    public DeserializationDTOMethod getDeserializationMethod() {
        return this.deserializationMethod;
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

    public DTOElements elements() {
        return theDTOElements(this.deserializationMethod.elements(this.type));
    }
}
