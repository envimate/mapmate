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

package com.envimate.mapmate.mapper.deserialization.deserializers;

import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.deserialization.DeserializerCallback;
import com.envimate.mapmate.mapper.deserialization.WrongInputStructureException;
import com.envimate.mapmate.mapper.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.mapper.injector.Injector;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ResolvedType;

import java.util.List;

public interface TypeDeserializer {
    List<ResolvedType> requiredTypes();

    <T> T deserialize(Universal input,
                      Definition definition,
                      ExceptionTracker exceptionTracker,
                      Injector injector,
                      DeserializerCallback callback,
                      CustomPrimitiveMappings customPrimitiveMappings);

    static <T extends Universal> T castSafely(final Universal universalType,
                                              final Class<T> type,
                                              final ExceptionTracker exceptionTracker) {
        if (!type.isInstance(universalType)) {
            throw WrongInputStructureException.wrongInputStructureException(type, universalType, exceptionTracker.getPosition());
        }
        return type.cast(universalType);
    }
}
