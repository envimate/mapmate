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

package com.envimate.mapmate.validation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ExceptionTracker {

    private final List<ValidationError> validationErrors;
    private final String position;
    private final ValidationMappings validationMappings;
    private final List<ExceptionTracker> children;
    private final Object originalInput;

    public ExceptionTracker(final Object originalInput, final ValidationMappings validationMappings) {
        this.originalInput = originalInput;
        this.validationErrors = new ArrayList<>(0);
        this.validationMappings = validationMappings;
        this.position = "";
        this.children = new ArrayList<>(0);
    }

    private ExceptionTracker(final Object originalInput, final String position, final ValidationMappings validationMappings) {
        this.originalInput = originalInput;
        this.validationErrors = new ArrayList<>(0);
        this.validationMappings = validationMappings;
        this.position = position;
        this.children = new ArrayList<>(0);
    }

    @SuppressWarnings({"CastToConcreteClass", "ThrowableNotThrown"})
    public void track(final Throwable e, final String messageProvidingDebugInformation) {
        final Throwable resolvedThrowable;
        if(e instanceof InvocationTargetException) {
            resolvedThrowable = ((InvocationTargetException) e).getTargetException();
        } else {
            resolvedThrowable = e;
        }
        final ExceptionMappingList exceptionMapping = this.validationMappings.get(resolvedThrowable.getClass());
        if (exceptionMapping != null) {
            final List<ValidationError> mapped = exceptionMapping.map(resolvedThrowable, this.position);
            this.validationErrors.addAll(mapped);
        } else {
            throw UnrecognizedExceptionOccurredException.fromException(
                    messageProvidingDebugInformation, this.position, resolvedThrowable, this.originalInput
            );
        }
    }

    public ExceptionTracker stepInto(final String position) {
        final ExceptionTracker exceptionTracker;
        if (this.position.equals("")) {
            exceptionTracker = new ExceptionTracker(this.originalInput, position, this.validationMappings);
        } else {
            exceptionTracker = new ExceptionTracker(this.originalInput, this.position + "." + position, this.validationMappings);
        }
        this.children.add(exceptionTracker);
        return exceptionTracker;
    }

    public List<ValidationError> resolve() {
        final List<ValidationError> flattened = this.flatten();
        return flattened;
    }

    public String getPosition() {
        return this.position;
    }

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    private List<ValidationError> flatten() {
        this.children.forEach(child -> {
            this.validationErrors.addAll(child.flatten());
        });

        return this.validationErrors;
    }

    public boolean hasValidationErrors() {
        return !this.validationErrors.isEmpty() || children.stream()
                .anyMatch(ExceptionTracker::hasValidationErrors);
    }

    public String getWouldBePosition(final String elementName) {
        if (this.position.equals("")) {
            return elementName;
        } else {
            return this.position + "." + elementName;
        }
    }

    public static final class ExceptionEntry {
        private final String from;
        private final String[] blamed;
        private final Throwable exception;

        ExceptionEntry(final String from, final String[] blamed, final Throwable e) {
            this.from = from;
            this.blamed = blamed.clone();
            this.exception = e;
        }

        public ExceptionEntry(final String from, final String blamed, final Throwable e) {
            this.from = from;
            this.blamed = new String[]{blamed};
            this.exception = e;
        }

        public String getFrom() {
            return this.from;
        }

        public String[] getBlamed() {
            return this.blamed.clone();
        }

        public Throwable getException() {
            return this.exception;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final ExceptionEntry that = (ExceptionEntry) obj;
            return Objects.equals(this.from, that.from) &&
                    Objects.equals(this.exception, that.exception);
        }

        @Override
        public int hashCode() {

            return Objects.hash(this.from, this.exception);
        }

        @Override
        public String toString() {
            return "ExceptionEntry{" +
                    "from='" + this.from + '\'' +
                    ", exception=" + this.exception +
                    '}';
        }
    }
}
