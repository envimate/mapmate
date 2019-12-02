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

package com.envimate.mapmate.mapper.deserialization;

import com.envimate.mapmate.shared.types.ClassType;

import static java.lang.String.format;

public final class UnknownReferenceException extends RuntimeException {
    private UnknownReferenceException(final String msg) {
        super(msg);
    }

    public static UnknownReferenceException fromType(final ClassType owner, final ClassType unknownType, final String dump) {
        final String msg = format(
                "definition '%s' contains unknown reference of type '%s'. All known definitions:%n%s",
                owner.description(),
                unknownType.description(),
                dump);
        return new UnknownReferenceException(msg);
    }
}
