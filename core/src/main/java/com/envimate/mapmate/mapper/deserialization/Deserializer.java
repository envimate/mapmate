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

package com.envimate.mapmate.mapper.deserialization;

import com.envimate.mapmate.mapper.definitions.Definitions;
import com.envimate.mapmate.mapper.definitions.universal.Universal;
import com.envimate.mapmate.mapper.definitions.universal.UniversalObject;
import com.envimate.mapmate.mapper.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.mapper.deserialization.validation.ValidationErrorsMapping;
import com.envimate.mapmate.mapper.deserialization.validation.ValidationMappings;
import com.envimate.mapmate.mapper.injector.Injector;
import com.envimate.mapmate.mapper.injector.InjectorFactory;
import com.envimate.mapmate.mapper.injector.InjectorLambda;
import com.envimate.mapmate.mapper.marshalling.MarshallerRegistry;
import com.envimate.mapmate.mapper.marshalling.MarshallingType;
import com.envimate.mapmate.mapper.marshalling.Unmarshaller;
import com.envimate.mapmate.shared.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.shared.types.ClassType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

import static com.envimate.mapmate.mapper.definitions.universal.UniversalObject.universalObjectFromNativeMap;
import static com.envimate.mapmate.mapper.deserialization.InternalDeserializer.internalDeserializer;
import static com.envimate.mapmate.mapper.deserialization.Unmarshallers.unmarshallers;
import static com.envimate.mapmate.mapper.deserialization.validation.ExceptionTracker.emptyTracker;
import static com.envimate.mapmate.mapper.injector.InjectorLambda.noop;
import static com.envimate.mapmate.mapper.marshalling.MarshallingType.json;
import static com.envimate.mapmate.shared.types.ClassType.fromClassWithoutGenerics;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deserializer {
    private final Definitions definitions;
    private final ValidationMappings validationMappings;
    private final Unmarshallers unmarshallers;
    private final InternalDeserializer internalDeserializer;
    private final InjectorFactory injectorFactory;

    public static Deserializer theDeserializer(final MarshallerRegistry<Unmarshaller> unmarshallerRegistry,
                                               final Definitions definitions,
                                               final CustomPrimitiveMappings customPrimitiveMappings,
                                               final ValidationMappings exceptionMapping,
                                               final ValidationErrorsMapping onValidationErrors,
                                               final InjectorFactory injectorFactory) {
        validateNotNull(unmarshallerRegistry, "unmarshallerRegistry");
        validateNotNull(definitions, "definitions");
        validateNotNull(customPrimitiveMappings, "customPrimitiveMappings");
        validateNotNull(exceptionMapping, "validationMappings");
        validateNotNull(onValidationErrors, "onValidationErrors");
        validateNotNull(injectorFactory, "injectorFactory");

        final Unmarshallers unmarshallers = unmarshallers(unmarshallerRegistry, definitions);
        final InternalDeserializer internalDeserializer = internalDeserializer(definitions, customPrimitiveMappings, onValidationErrors);
        return new Deserializer(definitions, exceptionMapping, unmarshallers, internalDeserializer, injectorFactory);
    }

    public <T> T deserializeFromMap(final Map<String, Object> input,
                                    final Class<T> targetType) {
        return deserializeFromMap(input, targetType, noop());
    }

    public <T> T deserializeFromMap(final Map<String, Object> input,
                                    final ClassType targetType) {
        return deserializeFromMap(input, targetType, noop());
    }

    public <T> T deserializeFromMap(final Map<String, Object> input,
                                    final Class<T> targetType,
                                    final InjectorLambda injectorProducer) {
        return deserializeFromMap(input, fromClassWithoutGenerics(targetType), injectorProducer);
    }

    public <T> T deserializeFromMap(final Map<String, Object> input,
                                    final ClassType targetType,
                                    final InjectorLambda injectorProducer) {
        final UniversalObject universalObject = universalObjectFromNativeMap(input);
        return deserialize(universalObject, targetType, injectorProducer);
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
        return deserialize(input, targetType, marshallingType, noop());
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(final String input,
                             final ClassType targetType,
                             final MarshallingType marshallingType) {
        return (T) deserialize(input, targetType, marshallingType, noop());
    }

    public <T> T deserialize(final String input,
                             final Class<T> targetType,
                             final MarshallingType marshallingType,
                             final InjectorLambda injectorProducer) {
        validateNotNull(input, "input");
        final Universal unmarshalled = this.unmarshallers.unmarshal(input, fromClassWithoutGenerics(targetType), marshallingType);
        return deserialize(unmarshalled, fromClassWithoutGenerics(targetType), injectorProducer);
    }

    public Object deserialize(final String input,
                              final ClassType targetType,
                              final MarshallingType marshallingType,
                              final InjectorLambda injectorProducer) {
        validateNotNull(input, "input");
        final Universal unmarshalled = this.unmarshallers.unmarshal(input, targetType, marshallingType);
        return deserialize(unmarshalled, targetType, injectorProducer);
    }

    private <T> T deserialize(final Universal input,
                              final ClassType targetType,
                              final InjectorLambda injectorProducer) {
        validateNotNull(input, "input");
        validateNotNull(targetType, "targetType");
        validateNotNull(injectorProducer, "jsonInjector");
        final ExceptionTracker exceptionTracker = emptyTracker(input, this.validationMappings);
        final Injector injector = this.injectorFactory.create();
        injectorProducer.setupInjector(injector);
        return this.internalDeserializer.deserialize(input, targetType, exceptionTracker, injector);
    }

    public Set<MarshallingType> supportedMarshallingTypes() {
        return this.unmarshallers.supportedMarshallingTypes();
    }

    public Definitions getDefinitions() {
        return this.definitions;
    }
}
