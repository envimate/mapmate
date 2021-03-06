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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeserializableCustomPrimitive<T> implements Definition {
    private final Class<T> type;
    private final DeserializationCPMethod deserializationMethod;

    public static <T> DeserializableCustomPrimitive<T> deserializableCustomPrimitive(
            final Class<T> type, final DeserializationCPMethod deserializationMethod) {
        deserializationMethod.verifyCompatibility(type);
        return new DeserializableCustomPrimitive<>(type, deserializationMethod);
    }

    @SuppressWarnings("unchecked")
    public T deserialize(final String input) throws Exception {
        final Object deserialized = this.deserializationMethod.deserialize(input, this.type);
        return (T) deserialized;
    }

    @Override
    public boolean isCustomPrimitive() {
        return true;
    }

    @Override
    public boolean isDataTransferObject() {
        return false;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }
}
