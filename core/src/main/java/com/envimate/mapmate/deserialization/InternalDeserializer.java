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
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.deserialization.validation.ValidationErrorsMapping;
import com.envimate.mapmate.deserialization.validation.ValidationResult;
import com.envimate.mapmate.injector.Injector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.envimate.mapmate.DefinitionNotFoundException.definitionNotFound;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Array.newInstance;

@SuppressWarnings({"unchecked", "InstanceofConcreteClass", "CastToConcreteClass", "rawtypes"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class InternalDeserializer {
    private final DeserializableDefinitions definitions;
    private final ValidationErrorsMapping onValidationErrors;

    static InternalDeserializer internalDeserializer(final DeserializableDefinitions deserializableDefinitions,
                                                     final ValidationErrorsMapping validationErrorsMapping) {
        validateNotNull(deserializableDefinitions, "deserializableDefinitions");
        validateNotNull(validationErrorsMapping, "validationErrorsMapping");
        return new InternalDeserializer(deserializableDefinitions, validationErrorsMapping);
    }

    <T> T deserialize(final Object input,
                      final Class<T> targetType,
                      final ExceptionTracker exceptionTracker,
                      final Injector injector) {
        final T result = deserializeRecursive(input, targetType, exceptionTracker, injector);
        final ValidationResult validationResult = exceptionTracker.validationResult();
        if (validationResult.hasValidationErrors()) {
            this.onValidationErrors.map(validationResult.validationErrors());
        }
        return result;
    }

    private <T> T deserializeRecursive(final Object input,
                                       final Class<T> targetType,
                                       final ExceptionTracker exceptionTracker,
                                       final Injector injector) {
        final Object injected = injector.getInjectionForPropertyPath(exceptionTracker.getPosition(), targetType).orElse(input);
        if (injected != null && injected.getClass() == targetType) {
            return (T) injected;
        }
        if (input == null) {
            return null;
        }

        if (injected instanceof List) {
            return deserializeArray((List) injected, targetType, exceptionTracker, injector);
        }
        final Definition definition = this.definitions.getDefinitionForType(targetType)
                .orElseThrow(() -> definitionNotFound(targetType));
        if (definition instanceof DeserializableDataTransferObject) {
            return deserializeDataTransferObject(
                    (Map<String, Object>) injected,
                    (DeserializableDataTransferObject) definition,
                    exceptionTracker,
                    injector);
        }
        if (definition instanceof DeserializableCustomPrimitive) {
            return deserializeCustomPrimitive(
                    (String) injected,
                    (DeserializableCustomPrimitive) definition,
                    exceptionTracker);
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
        final Map<String, Object> elements = new HashMap<>(0);
        for (final Entry<String, Class<?>> entry : elementTypes.entrySet()) {
            final String elementName = entry.getKey();
            final Class elementType = entry.getValue();

            final Object injected = injector.getInjectionForPropertyNameOrInstance(
                    exceptionTracker.getWouldBePosition(elementName), elementType);
            if (injected != null) {
                elements.put(elementName, injected);
            } else {
                final Object elementInput = input.get(elementName);
                if (elementInput != null) {
                    final Object elementObject = deserializeRecursive(
                            elementInput,
                            elementType,
                            exceptionTracker.stepInto(elementName),
                            injector);
                    elements.put(elementName, elementObject);
                }
            }
        }

        if (exceptionTracker.validationResult().hasValidationErrors()) {
            return null;
        } else {
            try {
                return (T) deserializationDTOMethod.deserialize(type, elements);
            } catch (final Exception e) {
                final String message = String.format(
                        "Exception calling deserialize(type: %s, elements: %s) on deserializationMethod %s",
                        type, elements, deserializationDTOMethod
                );
                exceptionTracker.track(e, message);
                return null;
            }
        }
    }

    private <T> T deserializeCustomPrimitive(final String input,
                                             final DeserializableCustomPrimitive definition,
                                             final ExceptionTracker exceptionTracker) {
        try {
            return (T) definition.deserialize(input);
        } catch (final Exception e) {
            final String message = String.format(
                    "Exception calling deserialize(input: %s) on definition %s",
                    input, definition
            );
            exceptionTracker.track(e, message);
            return null;
        }
    }

    private <T> T deserializeArray(final List input,
                                   final Class<T> targetType,
                                   final ExceptionTracker exceptionTracker,
                                   final Injector injector) {
        final Object[] output = (Object[]) newInstance(targetType.getComponentType(), input.size());

        for (int i = 0; i < input.size(); i++) {
            output[i] = deserializeRecursive(
                    input.get(i),
                    targetType.getComponentType(),
                    exceptionTracker.stepIntoArray(i),
                    injector);
        }

        return (T) output;
    }
}
