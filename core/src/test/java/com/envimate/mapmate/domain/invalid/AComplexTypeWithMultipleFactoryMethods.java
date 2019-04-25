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

package com.envimate.mapmate.domain.invalid;

import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;

public final class AComplexTypeWithMultipleFactoryMethods {
    public final AString stringA;
    public final AString stringB;
    public final ANumber number1;
    public final ANumber number2;

    private AComplexTypeWithMultipleFactoryMethods(final AString stringA,
                                                   final AString stringB,
                                                   final ANumber number1,
                                                   final ANumber number2) {
        this.stringA = stringA;
        this.stringB = stringB;
        this.number1 = number1;
        this.number2 = number2;
    }

    public static AComplexTypeWithMultipleFactoryMethods aFactoryMethod1(
            final AString stringA,
            final AString stringB,
            final ANumber number1,
            final ANumber number2) {
        return new AComplexTypeWithMultipleFactoryMethods(stringA, stringB, number1, number2);
    }

    public static AComplexTypeWithMultipleFactoryMethods aFactoryMethod2(
            final AString stringA,
            final AString stringB,
            final ANumber number1,
            final ANumber number2) {
        return new AComplexTypeWithMultipleFactoryMethods(stringA, stringB, number1, number2);
    }
}
