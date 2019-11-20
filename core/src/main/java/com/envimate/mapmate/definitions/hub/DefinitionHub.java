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

package com.envimate.mapmate.definitions.hub;

import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.definitions.Definition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static com.envimate.mapmate.definitions.hub.FullType.type;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionHub {
    private final Detector detector;

    public static DefinitionHub definitionHub(final Detector detector) {
        validateNotNull(detector, "detector");
        return new DefinitionHub(detector);
    }

    public Definition byType(final Class<?> type) {
        return this.detector.detect(type(type))
                .orElseThrow(); // TODO
    }

    public Definition byField(final Field field) {
        final Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            return byParameterizedType((ParameterizedType) genericType);
        }
        return byType(field.getType());
    }

    public Definition byParameterizedType(final ParameterizedType parameterizedType) {
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Arrays.stream(actualTypeArguments).forEach(type1 -> {
            System.out.println("type1 = " + type1);
        });
        throw new UnsupportedOperationException();
    }
}
