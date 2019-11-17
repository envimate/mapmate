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

import com.envimate.mapmate.definitions.Definition;
import com.envimate.mapmate.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.definitions.Definitions;
import com.envimate.mapmate.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
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

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Array.newInstance;

@SuppressWarnings({"unchecked", "InstanceofConcreteClass", "CastToConcreteClass", "rawtypes"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class InternalDeserializer {
    private final Definitions definitions;
    private final ValidationErrorsMapping onValidationErrors;

    static InternalDeserializer internalDeserializer(final Definitions definitions,
                                                     final ValidationErrorsMapping validationErrorsMapping) {
        validateNotNull(definitions, "definitions");
        validateNotNull(validationErrorsMapping, "validationErrorsMapping");
        return new InternalDeserializer(definitions, validationErrorsMapping);
    }

    <T> T deserialize(final Object input,
                      final Class<T> targetType,
                      final ExceptionTracker exceptionTracker,
                      final Injector injector) {
        final T result = this.deserializeRecursive(input, targetType, exceptionTracker, injector);
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
            return this.deserializeArray((List) injected, targetType, exceptionTracker, injector);
        }
        final Definition definition = this.definitions.getDefinitionForType(targetType);
        if (definition instanceof SerializedObjectDefinition) {
            return this.deserializeDataTransferObject(
                    (Map<String, Object>) injected,
                    (SerializedObjectDefinition) definition,
                    exceptionTracker,
                    injector);
        }
        if (definition instanceof CustomPrimitiveDefinition) {
            return this.deserializeCustomPrimitive(
                    (String) injected,
                    (CustomPrimitiveDefinition) definition,
                    exceptionTracker);
        }
        throw new UnsupportedOperationException(definition.getClass().getName());
    }

    private <T> T deserializeDataTransferObject(final Map<String, Object> input,
                                                final SerializedObjectDefinition definition,
                                                final ExceptionTracker exceptionTracker,
                                                final Injector injector) {
        final SerializedObjectDeserializer deserializer = definition.deserializer();
        final Class type = definition.type();
        final DeserializationFields deserializationFields = deserializer.fields();
        final Map<String, Object> elements = new HashMap<>(0);
        for (final Entry<String, Class<?>> entry : deserializationFields.fields().entrySet()) {
            final String elementName = entry.getKey();
            final Class elementType = entry.getValue();

            final Object injected = injector.getInjectionForPropertyNameOrInstance(
                    exceptionTracker.getWouldBePosition(elementName), elementType);
            if (injected != null) {
                elements.put(elementName, injected);
            } else {
                final Object elementInput = input.get(elementName);
                if (elementInput != null) {
                    final Object elementObject = this.deserializeRecursive(
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
                return (T) deserializer.deserialize(type, elements);
            } catch (final Exception e) {
                final String message = String.format(
                        "Exception calling deserialize(type: %s, elements: %s) on deserializationMethod %s",
                        type, elements, deserializer
                );
                exceptionTracker.track(e, message);
                return null;
            }
        }
    }

    private <T> T deserializeCustomPrimitive(final String input,
                                             final CustomPrimitiveDefinition definition,
                                             final ExceptionTracker exceptionTracker) {
        try {
            return (T) definition.deserializer().deserialize(input);
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
            output[i] = this.deserializeRecursive(
                    input.get(i),
                    targetType.getComponentType(),
                    exceptionTracker.stepIntoArray(i),
                    injector);
        }

        return (T) output;
    }
}
