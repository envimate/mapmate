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

package com.envimate.mapmate.mapper.deserialization.validation;

public final class ValidationError {

    public final String message;
    public final String propertyPath;

    public ValidationError(final String message, final String propertyPath) {
        this.message = message;
        this.propertyPath = propertyPath;
    }

    public static ValidationError fromExceptionMessageAndPropertyPath(
            final Throwable throwable,
            final String propertyPath) {
        return new ValidationError(throwable.getMessage(), propertyPath);
    }

    public static ValidationError fromStringMessageAndPropertyPath(final String message, final String propertyPath) {
        return new ValidationError(message, propertyPath);
    }

}
