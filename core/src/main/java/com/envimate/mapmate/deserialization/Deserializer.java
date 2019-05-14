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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.builder.DeserializerBuilder;
import com.envimate.mapmate.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.deserialization.validation.ValidationErrorsMapping;
import com.envimate.mapmate.deserialization.validation.ValidationMappings;
import com.envimate.mapmate.injector.Injector;
import com.envimate.mapmate.injector.InjectorLambda;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

import static com.envimate.mapmate.DefinitionNotFoundException.definitionNotFound;
import static com.envimate.mapmate.deserialization.InternalDeserializer.internalDeserializer;
import static com.envimate.mapmate.deserialization.Unmarshallers.unmarshallers;
import static com.envimate.mapmate.deserialization.builder.DeserializerBuilder.aDeserializerBuilder;
import static com.envimate.mapmate.deserialization.validation.ExceptionTracker.emptyTracker;
import static com.envimate.mapmate.injector.Injector.empty;
import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deserializer {
    private final DeserializableDefinitions definitions;
    private final ValidationMappings validationMappings;
    private final Unmarshallers unmarshallers;
    private final InternalDeserializer internalDeserializer;

    public static Deserializer theDeserializer(final MarshallerRegistry<Unmarshaller> unmarshallerRegistry,
                                               final DeserializableDefinitions definitions,
                                               final ValidationMappings exceptionMapping,
                                               final ValidationErrorsMapping onValidationErrors,
                                               final boolean validateNoUnsupportedOutgoingReferences) {
        validateNotNull(unmarshallerRegistry, "unmarshallerRegistry");
        validateNotNull(definitions, "definitions");
        validateNotNull(exceptionMapping, "validationMappings");
        validateNotNull(onValidationErrors, "onValidationErrors");

        if (validateNoUnsupportedOutgoingReferences) {
            definitions.validateNoUnsupportedOutgoingReferences();
        }

        final Unmarshallers unmarshalles = unmarshallers(unmarshallerRegistry, definitions);
        final InternalDeserializer internalDeserializer = internalDeserializer(definitions, onValidationErrors);
        return new Deserializer(definitions, exceptionMapping, unmarshalles, internalDeserializer);
    }

    public static DeserializerBuilder aDeserializer() {
        return aDeserializerBuilder();
    }

    public <T> T deserializeFromMap(final Map<String, Object> input,
                                    final Class<T> targetType) {
        validateNotNull(input, "input");

        final Definition definition = this.definitions.getDefinitionForType(targetType)
                .orElseThrow(() -> definitionNotFound(targetType));

        if (!definition.isDataTransferObject()) {
            throw new UnsupportedOperationException("Only DTOs can be deserialized from map but found: " + definition);
        }
        final ExceptionTracker exceptionTracker = emptyTracker(input, this.validationMappings);
        return this.internalDeserializer.deserialize(input, targetType, exceptionTracker, empty());
    }

    public Map<String, Object> deserializeToMap(final String input,
                                                final MarshallingType type) {
        return this.unmarshallers.unmarshalToMap(input, type);
    }

    public <T> T deserializeJson(final String json,
                                 final Class<T> targetType) {
        return deserialize(json, targetType, json());
    }

    public <T> T deserializeJson(final String json,
                                 final Class<T> targetType,
                                 final InjectorLambda injectorLambda) {
        return deserialize(json, targetType, json(), injectorLambda);
    }

    public <T> T deserialize(final String input,
                             final Class<T> targetType,
                             final MarshallingType marshallingType) {
        return deserialize(input, targetType, marshallingType, InjectorLambda.noop());
    }

    public <T> T deserialize(final String input,
                             final Class<T> targetType,
                             final MarshallingType marshallingType,
                             final InjectorLambda injectorProducer) {
        validateNotNull(input, "originalInput");
        validateNotNull(targetType, "targetType");
        validateNotNull(injectorProducer, "jsonInjector");
        final ExceptionTracker exceptionTracker = emptyTracker(input, this.validationMappings);
        final Injector injector = empty();
        injectorProducer.setupInjector(injector);
        final Object unmarshalled = this.unmarshallers.unmarshal(input, targetType, marshallingType);
        return this.internalDeserializer.deserialize(unmarshalled, targetType, exceptionTracker, injector);
    }

    public Set<MarshallingType> supportedMarshallingTypes() {
        return this.unmarshallers.supportedMarshallingTypes();
    }

    public DeserializableDefinitions getDefinitions() {
        return this.definitions;
    }
}
