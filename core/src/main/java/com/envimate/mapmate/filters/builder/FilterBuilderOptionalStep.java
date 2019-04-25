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

package com.envimate.mapmate.filters.builder;

import com.envimate.mapmate.filters.ClassFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;

@SuppressWarnings("unchecked")
public final class FilterBuilderOptionalStep<T> implements FilterBuilderRequiredStep<T> {

    private final List<ClassFilter> classFilters;
    private final Function<List<ClassFilter>, T> resultConsumer;

    private FilterBuilderOptionalStep(final Function<List<ClassFilter>, T> resultConsumer) {
        this.classFilters = new LinkedList<>();
        this.resultConsumer = resultConsumer;
    }

    public static <T> FilterBuilderRequiredStep<T> aFilterBuilder(final Function<List<ClassFilter>, T> resultConsumer) {
        return new FilterBuilderOptionalStep<>(resultConsumer);
    }

    @Override
    public FilterBuilderOptionalStep<T> filteredBy(final ClassFilter filter) {
        validateNotNull(filter, "filter");
        this.classFilters.add(filter);
        return this;
    }

    @Override
    public FilterBuilderOptionalStep<T> identifiedByClassNamePrefix(final String... prefix) {
        return filteredBy(type -> stream(prefix).anyMatch(p -> type.getSimpleName().startsWith(p)));
    }

    @Override
    public FilterBuilderOptionalStep<T> identifiedByClassNameSuffix(final String... suffix) {
        return filteredBy(type -> stream(suffix).anyMatch(s -> type.getSimpleName().endsWith(s)));
    }

    @Override
    public FilterBuilderOptionalStep<T> excluding(final Class<?> excluded) {
        validateNotNull(excluded, "excluded");
        return filteredBy(type -> !type.equals(excluded));
    }

    public T thatAre() {
        return this.resultConsumer.apply(this.classFilters);
    }
}
