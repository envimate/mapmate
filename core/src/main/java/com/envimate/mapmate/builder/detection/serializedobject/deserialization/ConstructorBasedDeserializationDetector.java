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

package com.envimate.mapmate.builder.detection.serializedobject.deserialization;

import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.types.ClassType;
import com.envimate.types.resolver.ResolvedConstructor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.Common.findMatchingMethod;
import static com.envimate.mapmate.deserialization.deserializers.serializedobjects.ConstructorSerializedObjectDeserializer.createDeserializer;
import static com.envimate.types.resolver.ResolvedConstructor.resolvePublicConstructors;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructorBasedDeserializationDetector implements SerializedObjectDeserializationDetector {

    public static SerializedObjectDeserializationDetector constructorBased() {
        return new ConstructorBasedDeserializationDetector();
    }

    @Override
    public Optional<SerializedObjectDeserializer> detect(final ClassType type, final SerializationFields fields) {
        final List<ResolvedConstructor> constructors = resolvePublicConstructors(type);
        return findMatchingMethod(fields.typesList(), constructors, ResolvedConstructor::parameters)
                .map(constructor -> createDeserializer(type, constructor));
    }
}
