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

import com.envimate.mapmate.deserialization.builder.scanner.PackageScanner;
import com.envimate.mapmate.filters.builder.FilterBuilderRequiredStep;

import java.util.function.Function;

import static com.envimate.mapmate.deserialization.builder.CustomPrimitiveDeserializationMethodBuilder.aCustomPrimitiveDeserializationMethodBuilder;
import static com.envimate.mapmate.deserialization.builder.DataTransferObjectDeserializationMethodBuilder.aDataTransferObjectDeserializationMethodBuilder;
import static com.envimate.mapmate.deserialization.builder.scanner.CustomPrimitivePackageScanner.theCustomPrimitivePackageScanner;
import static com.envimate.mapmate.deserialization.builder.scanner.DataTransferObjectPackageScanner.theDataTransferObjectPackageScanner;
import static com.envimate.mapmate.filters.builder.FilterBuilderOptionalStep.aFilterBuilder;

public final class ScannablePackageBuilder {

    private final Function<PackageScanner, DeserializerBuilder> resultConsumer;

    private ScannablePackageBuilder(final Function<PackageScanner, DeserializerBuilder> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    static ScannablePackageBuilder aScannablePackageBuilder(final Function<PackageScanner, DeserializerBuilder> resultConsumer) {
        return new ScannablePackageBuilder(resultConsumer);
    }

    public FilterBuilderRequiredStep<CustomPrimitiveDeserializationMethodBuilder> forCustomPrimitives() {
        return aFilterBuilder(
                classFilters -> aCustomPrimitiveDeserializationMethodBuilder(
                        deserializationCPMethod -> this.resultConsumer.apply(
                                theCustomPrimitivePackageScanner(classFilters, deserializationCPMethod))));
    }

    public FilterBuilderRequiredStep<DataTransferObjectDeserializationMethodBuilder> forDataTransferObjects() {
        return aFilterBuilder(
                classFilters -> aDataTransferObjectDeserializationMethodBuilder(
                        deserializationDTOMethodFactory -> this.resultConsumer.apply(
                                theDataTransferObjectPackageScanner(classFilters, deserializationDTOMethodFactory))));
    }
}
