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

public class User {

    private final transient EventBus eventBus;

    public final AccountId accountId;
    public final UserName userName;
    public final Address address;

    private User(final EventBus eventBus, final AccountId accountId, final UserName userName, final Address address) {
        this.eventBus = eventBus;
        this.accountId = accountId;
        this.userName = userName;
        this.address = address;
    }

    public static final User user(
            final EventBus eventBus,
            final AccountId accountId,
            final UserName userName,
            final Address address) {
        return new User(eventBus, accountId, userName, address);
    }

    @Override
    public String toString() {
        return "User{" +
                "accountId=" + this.accountId +
                ", userName=" + this.userName +
                ", eventBus=" + Optional.ofNullable(this.eventBus).map(EventBus::name).orElse("null") +
                ", address=" + this.address +
                '}';
    }
}
