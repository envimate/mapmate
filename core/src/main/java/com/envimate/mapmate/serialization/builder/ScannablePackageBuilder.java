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

package com.envimate.mapmate.serialization.builder;

import com.envimate.mapmate.filters.builder.FilterBuilderRequiredStep;
import com.envimate.mapmate.serialization.builder.scanner.PackageScanner;

import java.util.function.Function;

import static com.envimate.mapmate.filters.builder.FilterBuilderOptionalStep.aFilterBuilder;
import static com.envimate.mapmate.serialization.builder.CustomPrimitiveSerializationMethodBuilder.aCustomPrimitiveSerializationMethodBuilder;
import static com.envimate.mapmate.serialization.builder.DataTransferObjectSerializationMethodBuilder.aDataTransferObjectSerializationMethodBuilder;
import static com.envimate.mapmate.serialization.builder.scanner.CustomPrimitivePackageScanner.theCustomPrimitivePackageScanner;
import static com.envimate.mapmate.serialization.builder.scanner.DataTransferObjectPackageScanner.theDataTransferObjectPackageScanner;

public final class ScannablePackageBuilder {

    private final Function<PackageScanner, SerializerBuilder> resultConsumer;

    private ScannablePackageBuilder(final Function<PackageScanner, SerializerBuilder> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    static ScannablePackageBuilder aScannablePackageBuilder(final Function<PackageScanner, SerializerBuilder> resultConsumer) {
        return new ScannablePackageBuilder(resultConsumer);
    }

    @SuppressWarnings("unchecked")
    public FilterBuilderRequiredStep<CustomPrimitiveSerializationMethodBuilder<Object>> forCustomPrimitives() {
        return aFilterBuilder(
                classFilters -> aCustomPrimitiveSerializationMethodBuilder(
                        Object.class, serializationCPMethod -> this.resultConsumer.apply(
                                theCustomPrimitivePackageScanner(classFilters, serializationCPMethod))));
    }

    @SuppressWarnings("unchecked")
    public FilterBuilderRequiredStep<DataTransferObjectSerializationMethodBuilder> forDataTransferObjects() {
        return aFilterBuilder(
                classFilters -> aDataTransferObjectSerializationMethodBuilder(
                        serializationDTOMethod -> this.resultConsumer.apply(
                                theDataTransferObjectPackageScanner(classFilters, serializationDTOMethod))));
    }
}
