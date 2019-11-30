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

import com.envimate.mapmate.definitions.types.FullType;
import com.envimate.mapmate.definitions.types.resolver.ResolvedMethod;
import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.Common.detectDeserializerMethods;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.Common.findMatchingMethod;
import static com.envimate.mapmate.deserialization.deserializers.serializedobjects.MethodSerializedObjectDeserializer.methodDeserializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamedMethodDeserializationDetector implements SerializedObjectDeserializationDetector {
    private final String deserializationMethodName;

    public static SerializedObjectDeserializationDetector namedMethodBased(final String deserializationMethodName) {
        validateNotNull(deserializationMethodName, "deserializationMethodName");
        return new NamedMethodDeserializationDetector(deserializationMethodName);
    }

    @Override
    public Optional<SerializedObjectDeserializer> detect(final FullType type, final SerializationFields fields) {
        final List<ResolvedMethod> resolvedMethods = detectDeserializerMethods(type);
        final List<ResolvedMethod> deserializerMethods = resolvedMethods.stream()
                .filter(method -> method.method().getName().equals(this.deserializationMethodName))
                .collect(toList());
        return findMatchingMethod(fields.typesList(), deserializerMethods, ResolvedMethod::parameters)
                .map(method -> methodDeserializer(type, method));
    }
}
