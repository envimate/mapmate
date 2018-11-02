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

package com.envimate.mapmate.reflections;

public final class FactoryMethodNotFoundException extends RuntimeException {
    private FactoryMethodNotFoundException(final String msg) {
        super(msg);
    }

    public static FactoryMethodNotFoundException factoryMethodNotFound(final Class<?> type) {
        final String msg = String.format("no factory method found on type '%s'", type);
        return new FactoryMethodNotFoundException(msg);
    }

    public static FactoryMethodNotFoundException factoryMethodNotFound(final Class<?> type, final String methodName) {
        final String msg = String.format("no factory method found on type '%s' with name '%s'", type, methodName);
        return new FactoryMethodNotFoundException(msg);
    }
}
