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

package com.envimate.mapmate.builder.conventional.customprimitives.methodannotation;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.builder.definitions.IncompatibleCustomPrimitiveException.incompatibleCustomPrimitiveException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomPrimitiveMethodAnnotationFactory implements CustomPrimitiveDefinitionFactory {
    public static CustomPrimitiveMethodAnnotationFactory customPrimitiveMethodAnnotationFactory() {
        return new CustomPrimitiveMethodAnnotationFactory();
    }

    @Override
    public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
        final Method[] typeMethods = type.getMethods();
        final List<Method> serializerMethods = Arrays.stream(typeMethods)
                .filter(method -> method.getAnnotationsByType(MapMatePrimitiveSerializer.class).length > 0)
                .collect(Collectors.toList());
        final List<Method> deserializerMethods = Arrays.stream(typeMethods)
                .filter(method -> method.getAnnotationsByType(MapMatePrimitiveDeserializer.class).length > 0)
                .collect(Collectors.toList());

        final int serializerAnnotationSize = serializerMethods.size();
        final int deserializerAnnotationSize = deserializerMethods.size();
        if (serializerAnnotationSize > 0 || deserializerAnnotationSize > 0) {
            if (!(serializerAnnotationSize == deserializerAnnotationSize && serializerAnnotationSize == 1)) {
                throw incompatibleCustomPrimitiveException(
                        "When using CustomPrimitiveSerializer and CustomPrimitiveDeserializer annotations, both need to" +
                                "be used exactly on one method. Found %s serializer annotation(s) and %s deserializer" +
                                "annotation(s) on type %s",
                        serializerAnnotationSize,
                        deserializerAnnotationSize,
                        type
                );
            }
            final Method serializationMethod = serializerMethods.get(0);
            final Method deserializationMethod = deserializerMethods.get(0);
            return Optional.of(customPrimitiveDefinition(type, serializationMethod, deserializationMethod));
        }
        return Optional.empty();
    }

}
