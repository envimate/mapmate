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

package com.envimate.mapmate.deserialization.specs.instances;

import com.envimate.mapmate.domain.valid.*;

import static com.envimate.mapmate.domain.valid.ANumber.fromInt;

public final class Instances {

    private Instances() {
    }

    public static Object theFullyInitializedExampleDto() {
        return AComplexType.aComplexType(
                AString.fromString("asdf"),
                AString.fromString("qwer"),
                ANumber.fromInt(1),
                ANumber.fromInt(5));
    }

    public static Object theFullyInitializedExampleDtoWithCollections() {
        final ANumber[] array = new ANumber[]{fromInt(1), fromInt(2)};
        return AComplexTypeWithArray.aComplexClassWithArrays(array);
    }

    public static Object theFullyInitializedNestedExampleDto() {
        return AComplexNestedType.aComplexNestedType(
                AComplexType.aComplexType(
                        AString.fromString("a"),
                        AString.fromString("b"),
                        ANumber.fromInt(1),
                        ANumber.fromInt(2)),
                AComplexType.aComplexType(
                        AString.fromString("c"),
                        AString.fromString("d"),
                        ANumber.fromInt(3),
                        ANumber.fromInt(4)));
    }
}
