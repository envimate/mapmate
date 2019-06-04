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

package com.envimate.mapmate.builder;

import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.Unmarshaller;
import com.envimate.mapmate.deserialization.builder.DeserializerBuilder;
import com.envimate.mapmate.deserialization.validation.ExceptionMappingWithPropertyPath;
import com.envimate.mapmate.deserialization.validation.ValidationError;
import com.envimate.mapmate.serialization.Marshaller;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed;
import static com.envimate.mapmate.filters.ClassFilters.havingFactoryMethodWithTheRightParameters;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateBuilder implements FirstStep,
        CustomPrimitiveExclusionConfigurationStep,
        ExceptionConfigurationStep,
        MapMateBuilderPublic,
        LastStep {

    private static final String DEFAULT_CUSTOM_PRIMITIVE_SERIALIZATION_METHOD_NAME = "stringValue";
    private String packageName;
    private String customPrimitiveSerializationMethodName = DEFAULT_CUSTOM_PRIMITIVE_SERIALIZATION_METHOD_NAME;
    private List<String> customPrimitiveExclusionPackages = new LinkedList<>();
    private List<String> customPrimitiveExclusionClasses = new LinkedList<>();
    private List<String> dtoExclusionPackages = new LinkedList<>();
    private List<String> dtoExclusionClasses = new LinkedList<>();
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private Class<? extends Throwable> exceptionIndicatingValidationError;
    private ExceptionMappingWithPropertyPath exceptionMapping = (exception, propertyPath) ->
            new ValidationError(exception.getMessage(), propertyPath);

    public static MapMateBuilderPublic mapMateBuilder() {
        return new MapMateBuilder();
    }

    @Override
    public FirstStep forPackage(final String packageName) {
        this.packageName = packageName;
        return this;
    }

    @Override
    public CustomPrimitiveExclusionConfigurationStep withCustomPrimitives() {
        return this;
    }

    @Override
    public CustomPrimitiveExclusionConfigurationStep serializedUsingMethodNamed(final String methodName) {
        this.customPrimitiveSerializationMethodName = methodName;
        return this;
    }

    @Override
    public CustomPrimitiveExclusionConfigurationStep excludingPackages(final String... packageNames) {
        this.customPrimitiveExclusionPackages = Arrays.asList(packageNames);
        return this;
    }

    @Override
    public CustomPrimitiveExclusionConfigurationStep excludingClasses(final String... classNames) {
        this.customPrimitiveExclusionClasses = Arrays.asList(classNames);
        return this;
    }

    @Override
    public ExceptionConfigurationStep usingMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        MapMateBuilder.this.marshaller = marshaller;
        MapMateBuilder.this.unmarshaller = unmarshaller;
        return MapMateBuilder.this;
    }

    @Override
    public DtoExclusionConfigurationStep withDtos() {
        return new DtoExclusionConfigurationStepImplementation();
    }

    @Override
    public LastStep withExceptionIndicatingValidationError(
            final Class<? extends Throwable> exceptionIndicatingValidationError) {
        return this.withExceptionIndicatingValidationError(
                exceptionIndicatingValidationError,
                this.exceptionMapping
        );
    }

    @Override
    public LastStep withExceptionIndicatingValidationError(
            final Class<? extends Throwable> exceptionIndicatingValidationError,
            final ExceptionMappingWithPropertyPath exceptionMapping) {
        this.exceptionIndicatingValidationError = exceptionIndicatingValidationError;
        this.exceptionMapping = exceptionMapping;
        return this;
    }

    @Override
    public MapMate build() {
        return MapMate.mapMate(aSerializer().thatScansThePackage(this.packageName)
                        .forCustomPrimitives()
                        .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed(
                                this.customPrimitiveSerializationMethodName)
                        )
                        .filteredBy(type -> !this.customPrimitiveExclusionPackages
                                .contains(type.getPackageName()) && !this.customPrimitiveExclusionClasses.contains(type)
                        )
                        .thatAre().serializedUsingTheMethodNamed(this.customPrimitiveSerializationMethodName)
                        .withJsonMarshaller(this.marshaller).thatScansThePackage(this.packageName)
                        .forDataTransferObjects().filteredBy(havingFactoryMethodWithTheRightParameters())
                        .filteredBy(type -> !this.dtoExclusionPackages.contains(type.getPackageName()) &&
                                !this.dtoExclusionClasses.contains(type))
                        .thatAre().serializedByItsPublicFields().withJsonMarshaller(this.marshaller)
                        .build(),
                this.buildDeserializer()
        );
    }

    private Deserializer buildDeserializer() {
        final DeserializerBuilder deserializerBuilder = aDeserializer()
                .thatScansThePackage(this.packageName)
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed(
                        this.customPrimitiveSerializationMethodName))
                .filteredBy(type -> !this.customPrimitiveExclusionPackages.contains(type.getPackageName()) &&
                        !this.customPrimitiveExclusionClasses.contains(type))
                .thatAre()
                .deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withJsonUnmarshaller(this.unmarshaller)
                .thatScansThePackage(this.packageName)
                .forDataTransferObjects()
                .filteredBy(havingFactoryMethodWithTheRightParameters())
                .filteredBy(type -> !this.dtoExclusionPackages.contains(type.getPackageName()) &&
                        !this.dtoExclusionClasses.contains(type))
                .thatAre()
                .deserializedUsingTheFactoryMethodWithTheRightParameters()

                .withJsonUnmarshaller(this.unmarshaller);
        if (this.exceptionIndicatingValidationError != null) {
            deserializerBuilder.mappingExceptionUsing(this.exceptionIndicatingValidationError, this.exceptionMapping);
        }
        return deserializerBuilder.build();
    }

    private class DtoExclusionConfigurationStepImplementation implements DtoExclusionConfigurationStep {
        @Override
        public DtoExclusionConfigurationStep excludingPackages(final String... packageNames) {
            MapMateBuilder.this.dtoExclusionPackages = Arrays.asList(packageNames);
            return this;
        }

        @Override
        public DtoExclusionConfigurationStep excludingClasses(final String... classNames) {
            MapMateBuilder.this.dtoExclusionClasses = Arrays.asList(classNames);
            return this;
        }

        @Override
        public ExceptionConfigurationStep usingMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {
            MapMateBuilder.this.usingMarshallers(marshaller, unmarshaller);
            return MapMateBuilder.this;
        }

        @Override
        public CustomPrimitiveExclusionConfigurationStep withCustomPrimitives() {
            return MapMateBuilder.this.withCustomPrimitives();
        }
    }
}

