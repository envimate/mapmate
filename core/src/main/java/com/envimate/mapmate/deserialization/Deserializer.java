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

package com.envimate.mapmate.deserialization;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.builder.DeserializerBuilder;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.injector.Injector;
import com.envimate.mapmate.injector.InjectorLambda;
import com.envimate.mapmate.validation.ExceptionTracker;
import com.envimate.mapmate.validation.ValidationError;
import com.envimate.mapmate.validation.ValidationErrorsMapping;
import com.envimate.mapmate.validation.ValidationMappings;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.envimate.mapmate.DefinitionNotFoundException.definitionNotFound;
import static com.envimate.mapmate.deserialization.builder.DeserializerBuilder.aDeserializerBuilder;
import static com.envimate.mapmate.injector.Injector.empty;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Array.newInstance;

@SuppressWarnings({"unchecked", "CastToConcreteClass", "rawtypes"})
public final class Deserializer {

    private static final Pattern COMPILE = Pattern.compile("\"");
    private final DeserializableDefinitions definitions;
    private final Unmarshaller unmarshaller;
    private final ValidationMappings validationMappings;
    private final ValidationErrorsMapping onValidationErrors;

    private Deserializer(final Unmarshaller unmarshaller,
                         final DeserializableDefinitions definitions,
                         final ValidationMappings validationMappings,
                         final ValidationErrorsMapping onValidationErrors) {
        this.unmarshaller = unmarshaller;
        this.definitions = definitions;
        this.validationMappings = validationMappings;
        this.onValidationErrors = onValidationErrors;
    }

    public static Deserializer theDeserializer(final Unmarshaller unmarshaller,
                                               final DeserializableDefinitions definitions,
                                               final ValidationMappings exceptionMapping,
                                               final ValidationErrorsMapping onValidationErrors,
                                               final boolean validateNoUnsupportedOutgoingReferences) {
        validateNotNull(unmarshaller, "unmarshaller");
        validateNotNull(definitions, "definitions");
        validateNotNull(exceptionMapping, "validationMappings");
        validateNotNull(onValidationErrors, "onValidationErrors");

        if (validateNoUnsupportedOutgoingReferences) {
            definitions.validateNoUnsupportedOutgoingReferences();
        }

        return new Deserializer(unmarshaller, definitions, exceptionMapping, onValidationErrors);
    }

    public static DeserializerBuilder aDeserializer() {
        return aDeserializerBuilder();
    }

    public <T> T deserialize(final String input, final Class<T> targetType) {
        return deserialize(input, targetType, injector -> injector);
    }

    public <T> T deserialize(final String input, final Class<T> targetType,
                             final InjectorLambda injectorProducer) {
        validateNotNull(input, "originalInput");
        validateNotNull(targetType, "targetType");
        validateNotNull(injectorProducer, "jsonInjector");
        final ExceptionTracker exceptionTracker = new ExceptionTracker(input, this.validationMappings);
        final Injector injector = injectorProducer.inject(empty());
        final T deserialized = deserialize(input, targetType, exceptionTracker, injector);
        final List<ValidationError> validationErrors = exceptionTracker.resolve();
        if (!validationErrors.isEmpty()) {
            this.onValidationErrors.map(validationErrors);
        }

        return deserialized;
    }

    private <T> T deserialize(final String input, final Class<T> targetType,
                              final ExceptionTracker exceptionTracker,
                              final Injector injector) {
        Objects.requireNonNull(input);
        if (input.isEmpty()) {
            return null;
        }

        final Definition definition = this.definitions.getDefinitionForType(targetType)
                .orElseThrow(() -> definitionNotFound(targetType));

        final String trimmedInput = input.trim();
        final Object inputObject;
        if (targetType.isArray() || Collection.class.isAssignableFrom(targetType)) {
            inputObject = this.unmarshaller.unmarshal(trimmedInput, List.class);
        } else if (definition.isDataTransferObject()) {
            inputObject = this.unmarshaller.unmarshal(trimmedInput, Map.class);
        } else if (definition.isCustomPrimitive()) {
            inputObject = COMPILE.matcher(trimmedInput).replaceAll("");
        } else {
            throw new UnsupportedOperationException(definition.getClass().getName());
        }

        return deserialize(inputObject, targetType, exceptionTracker, injector);
    }

    private <T> T deserialize(
            final Object input,
            final Class<T> targetType,
            final ExceptionTracker exceptionTracker,
            final Injector injector) {
        // inject here
        final Object injected = injector.getInjectionForPropertyPath(exceptionTracker.getPosition(), targetType);
        if (injected != null && injected.getClass() == targetType) {
            return (T) injected;
        }

        if (input instanceof List) {
            return deserializeArray((List) input, targetType, exceptionTracker, injector);
        }
        final Definition definition = this.definitions.getDefinitionForType(targetType)
                .orElseThrow(() -> definitionNotFound(targetType));
        if (definition instanceof DeserializableDataTransferObject) {
            final DeserializableDataTransferObject<T> deserializableDataTransferObject =
                    (DeserializableDataTransferObject) definition;

            if(injected != null) {
                return deserializeDataTransferObject(
                        (Map<String, Object>) injected,
                        deserializableDataTransferObject,
                        exceptionTracker,
                        injector);
            } else {
                return deserializeDataTransferObject(
                        (Map<String, Object>) input,
                        deserializableDataTransferObject,
                        exceptionTracker,
                        injector);
            }

        }
        if (definition instanceof DeserializableCustomPrimitive) {
            final DeserializableCustomPrimitive deserializableCustomPrimitive = (DeserializableCustomPrimitive) definition;

            if(injected != null) {
                return deserializeCustomPrimitive(
                        (String) injected,
                        deserializableCustomPrimitive,
                        exceptionTracker,
                        injector);
            } else {
                return deserializeCustomPrimitive(
                        (String) input,
                        deserializableCustomPrimitive,
                        exceptionTracker,
                        injector);
            }

        }
        throw new UnsupportedOperationException(definition.getClass().getName());
    }

    private <T> T deserializeDataTransferObject(final Map<String, Object> input,
                                                final DeserializableDataTransferObject definition,
                                                final ExceptionTracker exceptionTracker,
                                                final Injector injector) {
        final DeserializationDTOMethod deserializationDTOMethod = definition.getDeserializationMethod();
        final Class type = definition.getType();
        final Map<String, Class<?>> elementTypes = deserializationDTOMethod.elements(type);
        final Map<String, Object> elements = new HashMap<>();
        for (final Entry<String, Class<?>> entry : elementTypes.entrySet()) {
            final String elementName = entry.getKey();
            final Class elementType = entry.getValue();

            final Object injected = injector.getInjectionForPropertyNameOrInstance(
                    exceptionTracker.getWouldBePosition(elementName), elementType);
            if(injected != null) {
                elements.put(elementName, injected);
            } else {
                final Object elementInput = input.get(elementName);
                if (elementInput != null) {
                    final Object elementObject = deserialize(
                            elementInput,
                            elementType,
                            exceptionTracker.stepInto(elementName),
                            injector);
                    elements.put(elementName, elementObject);
                }
            }
        }

        if (exceptionTracker.hasValidationErrors()) {
            return null;
        } else {
            try {
                return (T) deserializationDTOMethod.deserialize(type, elements);
            } catch (final Exception e) {
                exceptionTracker.track(e);
                return null;
            }
        }
    }

    private <T> T deserializeCustomPrimitive(final String input,
                                             final DeserializableCustomPrimitive definition,
                                             final ExceptionTracker exceptionTracker, final Injector injector) {
        try {
            return (T) definition.deserialize(input);
        } catch (final Exception e) {
            exceptionTracker.track(e);
            return null;
        }
    }

    private <T> T deserializeArray(
            final List input,
            final Class<T> targetType,
            final ExceptionTracker exceptionTracker,
            final Injector injector) {
        final Object[] output = (Object[]) newInstance(targetType.getComponentType(), input.size());

        for (int i = 0; i < input.size(); i++) {
            final Object value = input.get(i);
            final String indexIndication = String.format("[%s]", i);
            if (value instanceof Map) {
                output[i] = deserialize(
                        (Map) input.get(i),
                        targetType.getComponentType(),
                        exceptionTracker.stepInto(indexIndication),
                        injector);
            } else {
                output[i] = deserialize(
                        (String) input.get(i),
                        targetType.getComponentType(),
                        exceptionTracker.stepInto(indexIndication),
                        injector);
            }
        }

        return (T) output;
    }

    public DeserializableDefinitions getDefinitions() {
        return this.definitions;
    }
}
