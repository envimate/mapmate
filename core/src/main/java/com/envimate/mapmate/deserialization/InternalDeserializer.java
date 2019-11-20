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

import com.envimate.mapmate.definitions.*;
import com.envimate.mapmate.definitions.hub.FullType;
import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.deserialization.validation.ValidationErrorsMapping;
import com.envimate.mapmate.deserialization.validation.ValidationResult;
import com.envimate.mapmate.injector.Injector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.envimate.mapmate.definitions.hub.FullType.type;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

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
        final T result = (T) this.deserializeRecursive(input, type(targetType), exceptionTracker, injector);
        final ValidationResult validationResult = exceptionTracker.validationResult();
        if (validationResult.hasValidationErrors()) {
            this.onValidationErrors.map(validationResult.validationErrors());
        }
        return result;
    }

    private Object deserializeRecursive(final Object input,
                                        final FullType targetType,
                                        final ExceptionTracker exceptionTracker,
                                        final Injector injector) {
        final Object injected = injector.getInjectionForPropertyPath(exceptionTracker.getPosition(), targetType).orElse(input);
        if (injected != null && type(injected.getClass()).equals(targetType)) {
            return injected;
        }
        if (input == null) {
            return null;
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
        if (definition instanceof CollectionDefinition) {
            return this.deserializeArray(injected, (CollectionDefinition) definition, exceptionTracker, injector);
        }
        throw new UnsupportedOperationException(definition.getClass().getName());
    }

    private <T> T deserializeDataTransferObject(final Map<String, Object> input,
                                                final SerializedObjectDefinition definition,
                                                final ExceptionTracker exceptionTracker,
                                                final Injector injector) {
        final SerializedObjectDeserializer deserializer = definition.deserializer();
        final DeserializationFields deserializationFields = deserializer.fields();
        final Map<String, Object> elements = new HashMap<>(0);
        for (final Entry<String, FullType> entry : deserializationFields.fields().entrySet()) {
            final String elementName = entry.getKey();
            final FullType elementType = entry.getValue();

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
            return null; // TODO
        } else {
            try {
                return (T) deserializer.deserialize(elements);
            } catch (final Exception e) {
                final String message = String.format(
                        "Exception calling deserialize(type: %s, elements: %s) on deserializationMethod %s",
                        definition.type().description(), elements, deserializer
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

    private Object deserializeArray(final Object input,
                                    final CollectionDefinition definition,
                                    final ExceptionTracker exceptionTracker,
                                    final Injector injector) {
        if (!(input instanceof List)) {
            // TODO
            throw new UnsupportedOperationException(
                    String.format("Requiring an input of type List at position '%s' but found '%s'",
                            exceptionTracker.getPosition(), input));
        }
        final List list = (List) input;

        final FullType contentType = definition.contentType();
        int index = 0;
        final List deserializedList = new LinkedList();
        for (final Object element : list) {
            final Object deserialized = deserializeRecursive(element, contentType, exceptionTracker.stepIntoArray(index), injector);
            deserializedList.add(deserialized);
            index = index + 1;
        }
        return definition.deserializer().deserialize(deserializedList);
    }
}
