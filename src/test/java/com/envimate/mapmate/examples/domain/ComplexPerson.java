/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

import java.util.List;

public final class ComplexPerson {
    public final List<FirstName> firstNames;
    public final List<Address> addresses;

    private ComplexPerson(final List<FirstName> firstNames, final List<Address> addresses) {
        this.firstNames = firstNames;
        this.addresses = addresses;
    }

    public static ComplexPerson person(final List<FirstName> firstNames, final List<Address> addresses) {
        return new ComplexPerson(firstNames, addresses);
    }

    @Override
    public String toString() {
        return "ComplexPerson{" +
                "firstNames=" + this.firstNames +
                ", addresses=" + this.addresses +
                '}';
    }
}
