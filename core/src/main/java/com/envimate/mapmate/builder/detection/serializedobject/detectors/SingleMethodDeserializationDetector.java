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

package com.envimate.mapmate.builder.detection.serializedobject.detectors;

import com.envimate.mapmate.builder.detection.serializedobject.SerializedObjectDeserializationDetector;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.Common.detectDeserializerMethods;
import static com.envimate.mapmate.builder.definitions.deserializers.SerializedObjectByMethodDeserializer.createDeserializer;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleMethodDeserializationDetector implements SerializedObjectDeserializationDetector {

    public static SerializedObjectDeserializationDetector singleMethodBased() {
        return new SingleMethodDeserializationDetector();
    }

    @Override
    public Optional<DeserializationDTOMethod> detect(final Class<?> type, final Field[] fields) {
        final List<Method> deserializerCandidates = detectDeserializerMethods(type);
        if (deserializerCandidates.size() == 1) {
            final Method method = deserializerCandidates.get(0);
            return of(createDeserializer(type, method));
        }
        return empty();
    }
}
