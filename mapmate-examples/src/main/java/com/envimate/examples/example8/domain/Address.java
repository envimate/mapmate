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

package com.envimate.examples.example8.domain;

import com.envimate.examples.example8.events.EventBus;

import java.util.Optional;

public class Address {

    public final Street street;
    public final City city;
    private final transient EventBus eventBus;

    private Address(final EventBus eventBus, final Street street, final City city) {
        this.eventBus = eventBus;
        this.street = street;
        this.city = city;
    }

    public static final Address address(final EventBus eventBus, final Street street, final City city) {
        return new Address(eventBus, street, city);
    }

    @Override
    public String toString() {
        return "Address{" +
                "eventBus=" + Optional.ofNullable(this.eventBus).map(EventBus::name).orElse("null") +
                ", street=" + street +
                ", city=" + city +
                '}';
    }
}
