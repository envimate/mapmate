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

package com.envimate.mapmate.examples.domain;

import java.util.Objects;

public final class FirstName {
    private static final int MIN_VALUE_LENGTH = 3;
    private final String value;

    private FirstName(final String value) {
        this.value = value;
    }

    public static FirstName fromString(final String value) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("value for firstname must not be null");
        }
        if (value.length() < MIN_VALUE_LENGTH) {
            throw new IllegalArgumentException("firstname must at least contain 3 characters");
        }
        return new FirstName(value);
    }

    public String internalValueForMapping() {
        return this.value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FirstName firstName = (FirstName) o;
        return Objects.equals(this.value, firstName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public String toString() {
        return "FirstName{" +
                "value='" + this.value + '\'' +
                '}';
    }
}
