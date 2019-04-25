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

package com.envimate.mapmate.domain.valid;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public final class ANumber implements Serializable {
    private static final int MAX_VALUE = 50;
    private final int value;

    private ANumber(final int value) {
        this.value = value;
    }

    public static ANumber fromInt(final int value) {
        return new ANumber(value);
    }

    public static ANumber fromString(final String value) {
        final Double number = Double.valueOf(value);
        if(number > MAX_VALUE) {
            throw AnException.anException("value cannot be over 50");
        }
        return new ANumber(number.intValue());
    }

    public String internalValueForMapping() {
        return String.valueOf(this.value);
    }

    @Override
    public String toString() {
        return "AScannableNumber{" +
                "value=" + this.value +
                '}';
    }

    public boolean isLowerThen(final int value) {
        return this.value < value;
    }
}
