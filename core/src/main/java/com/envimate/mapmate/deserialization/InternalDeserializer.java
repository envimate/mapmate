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

import com.envimate.mapmate.builder.detection.customprimitive.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.definitions.*;
import com.envimate.mapmate.definitions.types.ResolvedType;
import com.envimate.mapmate.definitions.universal.*;
import com.envimate.mapmate.deserialization.deserializers.customprimitives.CustomPrimitiveDeserializer;
import com.envimate.mapmate.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.deserialization.validation.ExceptionTracker;
import com.envimate.mapmate.deserialization.validation.ValidationErrorsMapping;
import com.envimate.mapmate.deserialization.validation.ValidationResult;
import com.envimate.mapmate.injector.Injector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.Map.Entry;

import static com.envimate.mapmate.definitions.universal.UniversalNull.universalNull;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.String.format;

@SuppressWarnings({"unchecked", "InstanceofConcreteClass", "CastToConcreteClass", "rawtypes"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class InternalDeserializer {
    private final Definitions definitions;
    private final CustomPrimitiveMappings customPrimitiveMappings;
    private final ValidationErrorsMapping onValidationErrors;

    static InternalDeserializer internalDeserializer(final Definitions definitions,
                                                     final CustomPrimitiveMappings customPrimitiveMappings,
                                                     final ValidationErrorsMapping validationErrorsMapping) {
        validateNotNull(definitions, "definitions");
        validateNotNull(customPrimitiveMappings, "customPrimitiveMappings");
        validateNotNull(validationErrorsMapping, "validationErrorsMapping");
        return new InternalDeserializer(definitions, customPrimitiveMappings, validationErrorsMapping);
    }

    <T> T deserialize(final Universal input,
                      final ResolvedType targetType,
                      final ExceptionTracker exceptionTracker,
                      final Injector injector) {
        final T result = (T) this.deserializeRecursive(input, targetType, exceptionTracker, injector);
        final ValidationResult validationResult = exceptionTracker.validationResult();
        if (validationResult.hasValidationErrors()) {
            this.onValidationErrors.map(validationResult.validationErrors());
        }
        return result;
    }

    private Object deserializeRecursive(final Universal input,
                                        final ResolvedType targetType,
                                        final ExceptionTracker exceptionTracker,
                                        final Injector injector) {
        final Optional<Object> namedDirectInjection = injector.getDirectInjectionForPropertyPath(exceptionTracker.getPosition());
        if (namedDirectInjection.isPresent()) {
            return namedDirectInjection.get();
        }

        final Optional<Object> typedDirectInjection = injector.getDirectInjectionForType(targetType);
        if(typedDirectInjection.isPresent()) {
            return typedDirectInjection.get();
        }

        final Universal resolved = injector.getUniversalInjectionFor(exceptionTracker.getPosition()).orElse(input);

        if (input instanceof UniversalNull) {
            return null;
        }

        final Definition definition = this.definitions.getDefinitionForType(targetType);
        try {
            if (definition instanceof SerializedObjectDefinition) {
                return this.deserializeDataTransferObject(
                        castSafely(resolved, UniversalObject.class, exceptionTracker),
                        (SerializedObjectDefinition) definition,
                        exceptionTracker,
                        injector);
            }
            if (definition instanceof CustomPrimitiveDefinition) {
                return this.deserializeCustomPrimitive(
                        castSafely(resolved, UniversalPrimitive.class, exceptionTracker),
                        (CustomPrimitiveDefinition) definition,
                        exceptionTracker);
            }
            if (definition instanceof CollectionDefinition) {
                return this.deserializeCollection(
                        castSafely(resolved, UniversalCollection.class, exceptionTracker),
                        (CollectionDefinition) definition,
                        exceptionTracker,
                        injector);
            }
        } catch (final WrongInputStructureException e) {
            exceptionTracker.track(e, e.getMessage());
            return null;
        }
        throw new UnsupportedOperationException(definition.getClass().getName());
    }

    private <T> T deserializeDataTransferObject(final UniversalObject input,
                                                final SerializedObjectDefinition definition,
                                                final ExceptionTracker exceptionTracker,
                                                final Injector injector) {
        final SerializedObjectDeserializer deserializer = definition.deserializer()
                .orElseThrow(() -> new UnsupportedOperationException(format("No deserializer configured for '%s'", definition.type().description())));
        final DeserializationFields deserializationFields = deserializer.fields();
        final Map<String, Object> elements = new HashMap<>(0);
        for (final Entry<String, ResolvedType> entry : deserializationFields.fields().entrySet()) {
            final String elementName = entry.getKey();
            final ResolvedType elementType = entry.getValue();

            final Universal elementInput = input.getField(elementName).orElse(universalNull());
            final Object elementObject = this.deserializeRecursive(
                    elementInput,
                    elementType,
                    exceptionTracker.stepInto(elementName),
                    injector);
            elements.put(elementName, elementObject);
        }

        if (exceptionTracker.validationResult().hasValidationErrors()) {
            return null;
        } else {
            try {
                return (T) deserializer.deserialize(elements);
            } catch (final Exception e) {
                final String message = format("Exception calling deserialize(type: %s, elements: %s) on deserializationMethod %s",
                        definition.type().description(), elements, deserializer);
                exceptionTracker.track(e, message);
                return null;
            }
        }
    }

    private <T> T deserializeCustomPrimitive(final UniversalPrimitive input,
                                             final CustomPrimitiveDefinition definition,
                                             final ExceptionTracker exceptionTracker) {
        try {
            final CustomPrimitiveDeserializer deserializer = definition.deserializer().orElseThrow(() ->
                    new UnsupportedOperationException(String.format(
                            "Could not find deserializer for definition %s deserializing %s",
                            definition, input
                    )));
            final Class<?> baseType = deserializer.baseType();
            final Object mapped = this.customPrimitiveMappings.fromUniversal(input, baseType);
            return (T) deserializer.deserialize(mapped);
        } catch (final Exception e) {
            final String message = format("Exception calling deserialize(input: %s) on definition %s", input.toNativeJava(), definition);
            exceptionTracker.track(e, message);
            return null;
        }
    }

    private Object deserializeCollection(final UniversalCollection input,
                                         final CollectionDefinition definition,
                                         final ExceptionTracker exceptionTracker,
                                         final Injector injector) {
        final List deserializedList = new LinkedList();
        final ResolvedType contentType = definition.contentType();
        int index = 0;
        for (final Universal element : input.content()) {
            final Object deserialized = deserializeRecursive(element, contentType, exceptionTracker.stepIntoArray(index), injector);
            deserializedList.add(deserialized);
            index = index + 1;
        }
        return definition.deserializer().deserialize(deserializedList);
    }

    private static <T extends Universal> T castSafely(final Universal universalType,
                                                      final Class<T> type,
                                                      final ExceptionTracker exceptionTracker) {
        if (!type.isInstance(universalType)) {
            throw WrongInputStructureException.wrongInputStructureException(type, universalType, exceptionTracker.getPosition());
        }
        return (T) universalType;
    }
}
