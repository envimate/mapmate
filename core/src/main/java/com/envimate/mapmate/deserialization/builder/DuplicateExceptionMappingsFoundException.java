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

package com.envimate.mapmate.deserialization.builder;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

final class DuplicateExceptionMappingsFoundException extends RuntimeException {
    private final Set<Class<? extends Throwable>> duplicateMappedExceptions;

    private DuplicateExceptionMappingsFoundException(final String msg, final Set<Class<? extends Throwable>> duplicates) {
        super(msg);
        this.duplicateMappedExceptions = duplicates;
    }

    static DuplicateExceptionMappingsFoundException fromSet(final Set<Class<? extends Throwable>> duplicates) {
        final String msg = String.format("found exceptions with multiple mappings: %s", duplicates.stream()
                .map(Class::getName)
                .collect(Collectors.joining(", ")));

        return new DuplicateExceptionMappingsFoundException(msg, duplicates);
    }

    public Set<Class<? extends Throwable>> getDuplicateMappedExceptions() {
        return Collections.unmodifiableSet(duplicateMappedExceptions);
    }
}
