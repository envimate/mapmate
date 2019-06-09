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

import com.envimate.mapmate.builder.conventional.ConventionalDetector;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.deserialization.*;
import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.deserialization.validation.*;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.*;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.DefaultPackageScanner.defaultPackageScanner;
import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.deserialization.DeserializableDataTransferObject.deserializableDataTransferObject;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.deserializableDefinitions;
import static com.envimate.mapmate.deserialization.Deserializer.theDeserializer;
import static com.envimate.mapmate.marshalling.MarshallerRegistry.marshallerRegistry;
import static com.envimate.mapmate.serialization.SerializableCustomPrimitive.serializableCustomPrimitive;
import static com.envimate.mapmate.serialization.SerializableDataTransferObject.serializableDataTransferObject;
import static com.envimate.mapmate.serialization.SerializableDefinitions.serializableDefinitions;
import static com.envimate.mapmate.serialization.Serializer.theSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateBuilder {
    Map<MarshallingType, Marshaller> marshallerMap = new HashMap<>(1);
    Map<MarshallingType, Unmarshaller> unmarshallerMap = new HashMap<>(1);
    private Class<? extends Throwable> exceptionIndicatingValidationError;
    private ExceptionMappingWithPropertyPath exceptionMapping = (exception, propertyPath) ->
            new ValidationError(exception.getMessage(), propertyPath);
    private Detector detector = ConventionalDetector.conventionalDetectorWithAnnotations();
    private PackageScanner packageScanner;

    private final ValidationErrorsMapping validationErrorsMapping = validationErrors -> {
        throw AggregatedValidationException.fromList(validationErrors);
    };

    private final Map<Class<?>, CustomPrimitiveDefinition> manuallyAddedCustomPrimitives = new HashMap<>(1);
    private final Map<Class<?>, SerializedObjectDefinition> manuallyAddedSerializedObjects = new HashMap<>(1);

    public MapMateBuilder(final PackageScanner packageScanner) {
        this.packageScanner = packageScanner;
    }

    // TODO introduce events/callbacks to be more informative which packages have been scanned/callses identified.


    public static MapMateBuilder mapMateBuilder(final String... packageNames) {
        final List<String> packageNameList = Optional.ofNullable(packageNames)
                .map(Arrays::asList)
                .orElse(new LinkedList<>());
        final PackageScanner packageScanner = defaultPackageScanner(packageNameList);

        return new MapMateBuilder(packageScanner);
    }

    public static MapMateBuilder mapMateBuilder(final PackageScanner packageScanner) {
        validateNotNull(packageScanner, "packageScanner");
        return new MapMateBuilder(packageScanner);
    }

    public MapMateBuilder withDetector(final Detector detector) {
        this.detector = detector;
        return this;
    }

    public MapMateBuilder usingJsonMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "jsonMarshaller");
        validateNotNull(unmarshaller, "jsonUnmarshaller");
        this.marshallerMap.put(MarshallingType.json(), marshaller);
        this.unmarshallerMap.put(MarshallingType.json(), unmarshaller);
        return this;
    }

    public MapMateBuilder usingYamlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "yamlMarshaller");
        validateNotNull(unmarshaller, "yamlUnmarshaller");
        this.marshallerMap.put(MarshallingType.yaml(), marshaller);
        this.unmarshallerMap.put(MarshallingType.yaml(), unmarshaller);
        return this;
    }

    public MapMateBuilder usingXmlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "xmlMarshaller");
        validateNotNull(unmarshaller, "xmlUnmarshaller");
        this.marshallerMap.put(MarshallingType.xml(), marshaller);
        this.unmarshallerMap.put(MarshallingType.xml(), unmarshaller);
        return this;
    }

    public MapMateBuilder usingMarshaller(final MarshallingType marshallingType, final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "marshaller");
        validateNotNull(unmarshaller, "unmarshaller");
        validateNotNull(marshallingType, "marshallingType");
        this.marshallerMap.put(marshallingType, marshaller);
        this.unmarshallerMap.put(marshallingType, unmarshaller);
        return this;
    }

    public MapMateBuilder usingMarshallers(final Map<MarshallingType, Marshaller> marshallerMap,
                                           final Map<MarshallingType, Unmarshaller> unmarshallerMap) {
        this.marshallerMap = marshallerMap;
        this.unmarshallerMap = unmarshallerMap;
        return this;
    }

    public MapMateBuilder withExceptionIndicatingValidationError(
            final Class<? extends Throwable> exceptionIndicatingValidationError) {
        return this.withExceptionIndicatingValidationError(
                exceptionIndicatingValidationError,
                this.exceptionMapping
        );
    }

    public MapMateBuilder withExceptionIndicatingValidationError(
            final Class<? extends Throwable> exceptionIndicatingValidationError,
            final ExceptionMappingWithPropertyPath exceptionMapping) {
        this.exceptionIndicatingValidationError = exceptionIndicatingValidationError;
        this.exceptionMapping = exceptionMapping;
        return this;
    }

    public MapMateBuilder withCustomPrimitive(final CustomPrimitiveDefinition customPrimitive) {
        final CustomPrimitiveDefinition alreadyAdded = this.manuallyAddedCustomPrimitives.put(customPrimitive.type, customPrimitive);
        if (alreadyAdded != null) {
            throw new UnsupportedOperationException(String.format(
                    "The customPrimitive %s has already been added for type %s and is %s",
                    customPrimitive,
                    customPrimitive.type,
                    alreadyAdded));
        }

        return this;
    }

    public MapMateBuilder withSerializedObject(final SerializedObjectDefinition serializedObject) {
        final SerializedObjectDefinition alreadyAdded = this.manuallyAddedSerializedObjects.put(serializedObject.type,
                serializedObject);
        if (alreadyAdded != null) {
            throw new UnsupportedOperationException(String.format(
                    "The serializedObject %s has already been added for type %s and is %s",
                    serializedObject,
                    serializedObject.type,
                    alreadyAdded));
        }
        return this;
    }

    public MapMate build() {
        final MarshallerRegistry<Marshaller> marshallerRegistry = marshallerRegistry(this.marshallerMap);
        final MarshallerRegistry<Unmarshaller> unmarshallerRegistry = marshallerRegistry(this.unmarshallerMap);

        final List<Class<?>> scannedClasses = this.packageScanner.scan();
        final List<Class<?>> withoutManualClasses = scannedClasses.stream()
                .filter(aClass -> !this.manuallyAddedCustomPrimitives.containsKey(aClass) &&
                        !this.manuallyAddedSerializedObjects.containsKey(aClass))
                .collect(Collectors.toList());

        final List<CustomPrimitiveDefinition> customPrimitiveDefinitions = this.detector.customPrimitives(withoutManualClasses);
        final List<Class<?>> withoutCustomPrimitives = withoutManualClasses.stream()
                .filter(aClass -> customPrimitiveDefinitions.stream().noneMatch(d -> d.type == aClass))
                .collect(Collectors.toList());

        final List<SerializedObjectDefinition> serializedObjectDefinitions = this.detector.serializedObjects(withoutCustomPrimitives);

        final Set<DeserializableCustomPrimitive<?>> deserializableCustomPrimitives = new HashSet<>(customPrimitiveDefinitions.size());
        final Set<SerializableCustomPrimitive> serializableCustomPrimitives = new HashSet<>(customPrimitiveDefinitions.size());
        final Set<DeserializableDataTransferObject<?>> deserializableDataTransferObjects = new HashSet<>(serializedObjectDefinitions.size());
        final Set<SerializableDataTransferObject> serializableDataTransferObjects = new HashSet<>(serializedObjectDefinitions.size());

        for (final CustomPrimitiveDefinition customPrimitiveDefinition : customPrimitiveDefinitions) {
            final Class<?> type = customPrimitiveDefinition.type;
            final DeserializationCPMethod deserializer = customPrimitiveDefinition.deserializer;
            final SerializationCPMethod serializer = customPrimitiveDefinition.serializer;

            final DeserializableCustomPrimitive<?> deserializableCustomPrimitive = deserializableCustomPrimitive(type, deserializer);
            deserializableCustomPrimitives.add(deserializableCustomPrimitive);

            final SerializableCustomPrimitive serializableCustomPrimitive = serializableCustomPrimitive(type, serializer);
            serializableCustomPrimitives.add(serializableCustomPrimitive);
        }

        for (final SerializedObjectDefinition serializedObjectDefinition : serializedObjectDefinitions) {
            final DeserializationDTOMethod deserializer = serializedObjectDefinition.deserializer;
            final SerializationDTOMethod serializer = serializedObjectDefinition.serializer;
            final Class<?> type = serializedObjectDefinition.type;

            if (deserializer != null) {
                final DeserializableDataTransferObject<?> deserializableDataTransferObject = deserializableDataTransferObject(type, deserializer);
                deserializableDataTransferObjects.add(deserializableDataTransferObject);
            }
            if (serializer != null) {
                final SerializableDataTransferObject serializableDataTransferObject = serializableDataTransferObject(type, serializer);
                serializableDataTransferObjects.add(serializableDataTransferObject);
            }
        }

        final SerializableDefinitions serializableDefinitions = serializableDefinitions(
                serializableCustomPrimitives,
                serializableDataTransferObjects
        );
        final DeserializableDefinitions deserializableDefinitions = deserializableDefinitions(
                deserializableCustomPrimitives,
                deserializableDataTransferObjects
        );

        final Serializer serializer = theSerializer(marshallerRegistry, serializableDefinitions);

        final ValidationMappings validationMappings = ValidationMappings.empty();
        if (this.exceptionIndicatingValidationError != null) {
            validationMappings.putOneToOne(this.exceptionIndicatingValidationError, this.exceptionMapping);
        }

        final Deserializer deserializer = theDeserializer(unmarshallerRegistry, deserializableDefinitions,
                validationMappings, this.validationErrorsMapping, false);

        return MapMate.mapMate(serializer, deserializer);
    }
}

