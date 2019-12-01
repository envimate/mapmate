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

package com.envimate.mapmate.specs.givenwhenthen;

import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.Definitions;
import com.envimate.mapmate.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.definitions.types.ClassType;
import com.envimate.mapmate.definitions.types.ResolvedType;
import com.envimate.mapmate.deserialization.validation.AggregatedValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.StringContains;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Then {
    private final ThenData thenData;

    public static Then then(final ThenData thenData) {
        return new Then(thenData);
    }

    public Then theDeserializedObjectIs(final Object expected) {
        assertThat(this.thenData.getDeserializationResult(), is(expected));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Then theDeserialiedObjectHas(final Class<T> type, final Predicate<T> predicate) {
        final Object deserializationResult = this.thenData.getDeserializationResult();
        assertThat(deserializationResult, instanceOf(type));
        assertThat(predicate.test((T) deserializationResult), is(true));
        return this;
    }

    public Then anExceptionIsThrownWithAMessageContaining(final String message) {
        assertThat(this.thenData.getException().getMessage(), StringContains.containsString(message));
        return this;
    }

    public Then noExceptionHasBeenThrown() {
        if (nonNull(this.thenData.getException())) {
            this.thenData.getException().printStackTrace();
        }
        assertThat(this.thenData.getException(), is(nullValue()));
        return this;
    }

    public Then anAggregatedExceptionHasBeenThrownWithNumberOfErrors(final int numberOfErrors) {
        assertThat(this.thenData.getException(), instanceOf(AggregatedValidationException.class));
        final AggregatedValidationException aggregatedValidationException = (AggregatedValidationException) this.thenData.getException();
        assertThat(aggregatedValidationException.getValidationErrors(), hasSize(numberOfErrors));
        return this;
    }

    public Then theDefinitionsContainExactlyTheCustomPrimitives(final Class<?>... types) {
        final Definitions definitions = this.thenData.getDefinitions();
        final List<Class<?>> actualTypes = stream(types)
                .map(ClassType::fromClassWithoutGenerics)
                .map(definitions::getOptionalDefinitionForType)
                .flatMap(Optional::stream)
                .filter(definition -> definition instanceof CustomPrimitiveDefinition)
                .map(Definition::type)
                .map(ResolvedType::assignableType)
                .collect(toList());
        assertThat(actualTypes, containsInAnyOrder(types));
        assertThat(definitions.countCustomPrimitives(), is(types.length));
        return this;
    }

    public Then theDefinitionsContainExactlyTheSerializedObjects(final Class<?>... types) {
        final Definitions definitions = this.thenData.getDefinitions();
        final List<Class<?>> actualTypes = stream(types)
                .map(ClassType::fromClassWithoutGenerics)
                .map(definitions::getOptionalDefinitionForType)
                .flatMap(Optional::stream)
                .filter(definition -> definition instanceof SerializedObjectDefinition)
                .map(Definition::type)
                .map(ResolvedType::assignableType)
                .collect(toList());
        assertThat(actualTypes, containsInAnyOrder(types));
        assertThat(definitions.countSerializedObjects(), is(types.length));
        return this;
    }

    public Then theSerializationResultWas(final String serialized) {
        assertThat(this.thenData.getSerializationResult(), is(serialized));
        return this;
    }
}
