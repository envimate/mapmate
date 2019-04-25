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

package com.envimate.mapmate.serialization;

import com.envimate.mapmate.domain.valid.*;
import org.junit.Before;
import org.junit.Test;

public final class CircularReferenceDetectorTest {
    private CircularReferenceDetector detector;

    @Before
    public void before() {
        this.detector = new CircularReferenceDetector();
    }

    @Test(expected = CircularReferenceException.class)
    public void givenObjectWithCircularReference_whenDetecting_thenThrowsError() {
        //given
        final ACyclicType given1 = ACyclicType.aCyclicType(AString.fromString("a"));
        final ACyclicType given2 = ACyclicType.aCyclicType(AString.fromString("b"));

        given1.aCyclicType = given2;
        given2.aCyclicType = given1;

        //when
        this.detector.detect(given1);
    }

    @Test
    public void givenObjectWithSameReferencesYetNotCircular_whenDetecting_thenNothingHappens() {
        //given
        final AComplexType complexType1 = AComplexType.aComplexType(
                AString.fromString("a"),
                AString.fromString("b"),
                ANumber.fromInt(1),
                ANumber.fromInt(2));

        final AComplexNestedType given = AComplexNestedType.aComplexNestedType(
                complexType1,
                complexType1);

        //when
        this.detector.detect(given);
    }
}
