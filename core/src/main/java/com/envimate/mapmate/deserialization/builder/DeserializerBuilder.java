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

import com.envimate.mapmate.deserialization.*;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.deserialization.validation.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.deserialization.DeserializableDataTransferObject.deserializableDataTransferObject;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.*;
import static com.envimate.mapmate.deserialization.Deserializer.theDeserializer;
import static com.envimate.mapmate.deserialization.builder.CustomPrimitiveDeserializationMethodBuilder.aCustomPrimitiveDeserializationMethodBuilder;
import static com.envimate.mapmate.deserialization.builder.DataTransferObjectDeserializationMethodBuilder.aDataTransferObjectDeserializationMethodBuilder;
import static com.envimate.mapmate.deserialization.builder.ScannablePackageBuilder.aScannablePackageBuilder;
import static com.envimate.mapmate.marshalling.MarshallerRegistry.marshallerRegistry;
import static com.envimate.mapmate.marshalling.MarshallingType.*;
import static com.envimate.mapmate.reflections.PackageName.fromString;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static com.envimate.mapmate.validators.RequiredStringValidator.validateNotNullNorEmpty;

public final class DeserializerBuilder {
    private final Map<MarshallingType, Unmarshaller> unmarshallers;
    private final List<DeserializableDefinitions> definitions;
    private final ValidationMappings validationMappings;
    private final ThrowableClassList mappedExceptions;
    private ValidationErrorsMapping onValidationErrors;
    private boolean validateNoUnsupportedOutgoingReferences;

    private DeserializerBuilder() {
        this.unmarshallers = new HashMap<>();
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

    public UnmarshallerStage unmarshallingTheType(final MarshallingType marshallingType) {
        validateNotNull(marshallingType, "marshallingType");
        return unmarshaller -> {
            validateNotNull(unmarshaller, "unmarshaller");
            this.unmarshallers.put(marshallingType, unmarshaller);
            return this;
        };
    }

    public DeserializerBuilder withJsonUnmarshaller(final Unmarshaller unmarshaller) {
        return unmarshallingTheType(json()).using(unmarshaller);
    }

    public DeserializerBuilder withXmlUnmarshaller(final Unmarshaller unmarshaller) {
        return unmarshallingTheType(xml()).using(unmarshaller);
    }

    public DeserializerBuilder withYamlUnmarshaller(final Unmarshaller unmarshaller) {
        return unmarshallingTheType(yaml()).using(unmarshaller);
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
        return aDataTransferObjectDeserializationMethodBuilder(deserializationDTOMethodFactory -> {
            final DeserializableDataTransferObject<?> dataTransferObject =
                    deserializableDataTransferObject(type, deserializationDTOMethodFactory.createFor(type));
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
        if (this.mappedExceptions.containsDuplicates()) {
            throw DuplicateExceptionMappingsFoundException.fromSet(this.mappedExceptions.getDuplicates());
        }

        DeserializableDefinitions allDefinitions = empty();
        for (final DeserializableDefinitions definitions : this.definitions) {
            allDefinitions = merge(allDefinitions, definitions);
        }
        allDefinitions = merge(allDefinitions, theSpeciallyTreatedCustomPrimitives());

        final MarshallerRegistry<Unmarshaller> marshallerRegistry = marshallerRegistry(this.unmarshallers);
        return theDeserializer(
                marshallerRegistry,
                allDefinitions,
                this.validationMappings,
                this.onValidationErrors,
                this.validateNoUnsupportedOutgoingReferences);
    }
}
