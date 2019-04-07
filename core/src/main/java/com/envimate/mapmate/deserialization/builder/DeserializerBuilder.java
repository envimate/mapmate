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

package com.envimate.mapmate.deserialization.builder;

import com.envimate.mapmate.deserialization.*;
import com.envimate.mapmate.validation.*;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.deserialization.DeserializableDataTransferObject.deserializableDataTransferObject;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.*;
import static com.envimate.mapmate.deserialization.Deserializer.theDeserializer;
import static com.envimate.mapmate.deserialization.builder.CustomPrimitiveDeserializationMethodBuilder.aCustomPrimitiveDeserializationMethodBuilder;
import static com.envimate.mapmate.deserialization.builder.DataTransferObjectDeserializationMethodBuilder.aDataTransferObjectDeserializationMethodBuilder;
import static com.envimate.mapmate.deserialization.builder.ScannablePackageBuilder.aScannablePackageBuilder;
import static com.envimate.mapmate.reflections.PackageName.fromString;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static com.envimate.mapmate.validators.RequiredStringValidator.validateNotNullNorEmpty;

public final class DeserializerBuilder {

    private Unmarshaller unmarshaller;
    private final List<DeserializableDefinitions> definitions;
    private final ValidationMappings validationMappings;
    private final ThrowableClassList mappedExceptions;
    private ValidationErrorsMapping onValidationErrors;
    private boolean validateNoUnsupportedOutgoingReferences;

    private DeserializerBuilder() {
        this.definitions = new LinkedList<>();
        this.validationMappings = ValidationMappings.empty();
        this.mappedExceptions = ThrowableClassList.empty();
        this.onValidationErrors = validationErrors -> {
            throw AggregatedValidationException.fromList(validationErrors);
        };
        this.validateNoUnsupportedOutgoingReferences = false;
    }

    public static DeserializerBuilder aDeserializerBuilder() {
        return new DeserializerBuilder();
    }

    public DeserializerBuilder withUnmarshaller(final Unmarshaller unmarshaller) {
        validateNotNull(unmarshaller, "unmarshaller");
        this.unmarshaller = unmarshaller;
        return this;
    }

    public ScannablePackageBuilder thatScansThePackage(final String packageName) {
        validateNotNullNorEmpty(packageName, "packageName");
        return aScannablePackageBuilder(packageScanner -> {
            final DeserializableDefinitions definitions = packageScanner.scan(fromString(packageName));
            this.definitions.add(definitions);
            return this;
        });
    }

    public CustomPrimitiveDeserializationMethodBuilder withCustomPrimitive(final Class<?> type) {
        validateNotNull(type, "type");
        return aCustomPrimitiveDeserializationMethodBuilder(deserializationCPMethod -> {
            final DeserializableCustomPrimitive<?> customPrimitive = deserializableCustomPrimitive(type, deserializationCPMethod);
            final DeserializableDefinitions definitions = withASingleCustomPrimitive(customPrimitive);
            this.definitions.add(definitions);
            return this;
        });
    }

    public DataTransferObjectDeserializationMethodBuilder withDataTransferObject(final Class<?> type) {
        validateNotNull(type, "type");
        return aDataTransferObjectDeserializationMethodBuilder(deserializationDTOMethod -> {
            final DeserializableDataTransferObject<?> dataTransferObject =
                    deserializableDataTransferObject(type, deserializationDTOMethod);
            final DeserializableDefinitions definitions = withASingleDataTransferObject(dataTransferObject);
            this.definitions.add(definitions);
            return this;
        });
    }

    public DeserializerBuilder mappingExceptionUsing(final Class<? extends Throwable> exceptionType,
                                                     final ExceptionMappingWithPropertyPath mapping) {
        validateNotNull(exceptionType, "exceptionType");
        validateNotNull(mapping, "mapping");
        this.validationMappings.putOneToOne(exceptionType, mapping);
        this.mappedExceptions.add(exceptionType);
        return this;
    }

    public DeserializerBuilder mappingExceptionUsingList(final Class<? extends Throwable> exceptionType,
                                                     final ExceptionMappingList mapping) {
        validateNotNull(exceptionType, "exceptionType");
        validateNotNull(mapping, "mapping");
        this.validationMappings.putOneToMany(exceptionType, mapping);
        this.mappedExceptions.add(exceptionType);
        return this;
    }

    public DeserializerBuilder onValidationErrors(final ValidationErrorsMapping mapping) {
        this.onValidationErrors = mapping;
        return this;
    }

    public DeserializerBuilder validateNoUnsupportedOutgoingReferences() {
        this.validateNoUnsupportedOutgoingReferences = true;
        return this;
    }

    public DeserializerBuilder validateNoUnsupportedOutgoingReferences(final boolean value) {
        this.validateNoUnsupportedOutgoingReferences = value;
        return this;
    }

    public Deserializer build() {
        if(this.mappedExceptions.containsDuplicates()) {
            throw DuplicateExceptionMappingsFoundException.fromSet(this.mappedExceptions.getDuplicates());
        }

        DeserializableDefinitions allDefinitions = empty();
        for (final DeserializableDefinitions definitions : this.definitions) {
            allDefinitions = merge(allDefinitions, definitions);
        }
        allDefinitions = merge(allDefinitions, theSpeciallyTreatedCustomPrimitives());

        return theDeserializer(
                this.unmarshaller,
                allDefinitions,
                this.validationMappings,
                this.onValidationErrors,
                this.validateNoUnsupportedOutgoingReferences);
    }
}