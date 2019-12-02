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

import com.envimate.mapmate.definitions.*;
import com.envimate.mapmate.definitions.universal.Universal;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.marshalling.Unmarshaller;
import com.envimate.types.ClassType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.envimate.mapmate.definitions.universal.UniversalCollection.universalCollectionFromNativeList;
import static com.envimate.mapmate.definitions.universal.UniversalNull.universalNull;
import static com.envimate.mapmate.definitions.universal.UniversalObject.universalObjectFromNativeMap;
import static com.envimate.mapmate.definitions.universal.UniversalPrimitive.universalPrimitive;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Unmarshallers {
    private static final Pattern PATTERN = Pattern.compile("\"");

    private final MarshallerRegistry<Unmarshaller> unmarshallers;
    private final Definitions definitions;

    static Unmarshallers unmarshallers(final MarshallerRegistry<Unmarshaller> unmarshallers,
                                       final Definitions definitions) {
        validateNotNull(unmarshallers, "unmarshallers");
        validateNotNull(definitions, "definitions");
        return new Unmarshallers(unmarshallers, definitions);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> unmarshalToMap(final String input,
                                       final MarshallingType marshallingType) {
        validateNotNull(input, "input");
        validateNotNull(marshallingType, "marshallingType");
        final Unmarshaller unmarshaller = this.unmarshallers.getForType(marshallingType);
        try {
            return unmarshaller.unmarshal(input, Map.class);
        } catch (final Exception e) {
            throw new UnsupportedOperationException(
                    format("Could not unmarshal map from input %s", input),
                    e
            );
        }
    }

    @SuppressWarnings({"InstanceofConcreteClass", "unchecked"})
    Universal unmarshal(final String input,
                        final ClassType targetType,
                        final MarshallingType marshallingType) {
        validateNotNull(input, "input");
        if (input.isEmpty()) {
            return universalNull();
        }
        final Unmarshaller unmarshaller = this.unmarshallers.getForType(marshallingType);
        final Definition definition = this.definitions.getDefinitionForType(targetType);

        final String trimmedInput = input.trim();
        if (definition instanceof CollectionDefinition) {
            try {
                return universalCollectionFromNativeList(unmarshaller.unmarshal(trimmedInput, List.class));
            } catch (final Exception e) {
                throw new UnsupportedOperationException(format("Could not unmarshal list from input %s", input), e);
            }
        } else if (definition instanceof SerializedObjectDefinition) {
            try {
                return universalObjectFromNativeMap(unmarshaller.unmarshal(trimmedInput, Map.class));
            } catch (final Exception e) {
                throw new UnsupportedOperationException(format("Could not unmarshal map from input %s", input), e);
            }
        } else if (definition instanceof CustomPrimitiveDefinition) {
            return universalPrimitive(PATTERN.matcher(trimmedInput).replaceAll(""));
        } else {
            throw new UnsupportedOperationException(definition.getClass().getName());
        }
    }

    Set<MarshallingType> supportedMarshallingTypes() {
        return this.unmarshallers.supportedTypes();
    }
}
