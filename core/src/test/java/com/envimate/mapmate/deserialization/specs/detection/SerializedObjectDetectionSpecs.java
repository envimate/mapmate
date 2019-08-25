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

package com.envimate.mapmate.deserialization.specs.detection;

import com.envimate.mapmate.builder.Detector;
import com.envimate.mapmate.builder.MapMate;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.google.gson.Gson;
import lombok.Data;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.MapMate.aMapMate;
import static com.envimate.mapmate.builder.conventional.serializedobject.namebased.DeserializerMethodNameBasedSerializedObjectFactory.deserializerMethodNameBasedSerializedObjectFactory;
import static com.envimate.mapmate.deserialization.specs.detection.SerializedObjectDetectionSpecs.Val.aVal;
import static com.envimate.mapmate.deserialization.specs.detection.SerializedObjectDetectionSpecs.ValState.aState;
import static org.junit.Assert.assertEquals;

public class SerializedObjectDetectionSpecs {
    @Test
    public void detectsSerializedObjectUsingFactoryMethodNamePattern() {
        final MapMate mapmate = aMapMate(ValState.class.getPackageName())
                .usingJsonMarshaller(ignored -> null, new Gson()::fromJson)
                .withDetector(new Detector() {
                    @Override
                    public List<CustomPrimitiveDefinition> customPrimitives(final List<Class<?>> classes) {
                        return List.of(CustomPrimitiveDefinition.customPrimitiveDefinition(
                                Val.class,
                                o -> o.stringValue(),
                                s -> aVal(s))
                        );
                    }

                    @Override
                    public List<SerializedObjectDefinition> serializedObjects(final List<Class<?>> classes) {
                        final var analyzer = deserializerMethodNameBasedSerializedObjectFactory("^an?\\p{Upper}.*$");
                        return classes.stream()
                                .map(analyzer::analyze)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList());
                    }
                })
                .build();

        final ValState expected = aState(aVal("s"));
        final ValState actual = mapmate.deserializeJson("{\"val\":\"s\"}", ValState.class);
        assertEquals(expected, actual);
    }

    @Data
    public static class ValState {
        private final Val val;

        public static ValState aState(final Val val) {
            return new ValState(val);
        }
    }

    @Data
    public static class Val {
        private final String value;

        public static Val aVal(final String s) {
            return new Val(s);
        }

        public String stringValue() {
            return value;
        }
    }
}
