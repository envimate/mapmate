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
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.deserialization.*;
import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.deserialization.methods.StaticMethodCPMethod;
import com.envimate.mapmate.deserialization.validation.*;
import com.envimate.mapmate.injector.InjectorFactory;
import com.envimate.mapmate.injector.InjectorLambda;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.*;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.DefaultPackageScanner.defaultPackageScanner;
import static com.envimate.mapmate.builder.MapMate.mapMate;
import static com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.builder.definitions.SerializedObjectDefinition.serializedObjectDefinition;
import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.deserialization.DeserializableDataTransferObject.deserializableDataTransferObject;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.deserializableDefinitions;
import static com.envimate.mapmate.deserialization.Deserializer.theDeserializer;
import static com.envimate.mapmate.injector.InjectorFactory.injectorFactory;
import static com.envimate.mapmate.marshalling.MarshallerRegistry.marshallerRegistry;
import static com.envimate.mapmate.serialization.SerializableCustomPrimitive.serializableCustomPrimitive;
import static com.envimate.mapmate.serialization.SerializableDataTransferObject.serializableDataTransferObject;
import static com.envimate.mapmate.serialization.SerializableDefinitions.serializableDefinitions;
import static com.envimate.mapmate.serialization.Serializer.theSerializer;
import static com.envimate.mapmate.serialization.methods.ProvidedMethodSerializationCPMethod.providedMethodSerializationCPMethod;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public final class MapMateBuilder {
    private final ValidationErrorsMapping validationErrorsMapping = validationErrors -> {
        throw AggregatedValidationException.fromList(validationErrors);
    };
    private final Map<Class<?>, CustomPrimitiveDefinition> manuallyAddedCustomPrimitives = new HashMap<>(1);
    private final Map<Class<?>, SerializedObjectDefinition> manuallyAddedSerializedObjects = new HashMap<>(1);
    private Map<MarshallingType, Marshaller> marshallerMap = new HashMap<>(1);
    private Map<MarshallingType, Unmarshaller> unmarshallerMap = new HashMap<>(1);
    private final ValidationMappings validationMappings = ValidationMappings.empty();
    private Detector detector = ConventionalDetector.conventionalDetectorWithAnnotations();
    private final PackageScanner packageScanner;
    private final List<Class<?>> manuallyAddedSerializedObjectTypes = new LinkedList<>();
    private final List<Class<?>> manuallyAddedCustomPrimitiveTypes = new LinkedList<>();

    private final List<DeserializableCustomPrimitive<?>> deserializableCPs = new LinkedList<>();
    private final List<SerializableCustomPrimitive> serializableCPs = new LinkedList<>();
    private final List<DeserializableDataTransferObject<?>> deserializableDTOs = new LinkedList<>();
    private final List<SerializableDataTransferObject> serializableDTOs = new LinkedList<>();
    private InjectorFactory injectorFactory = InjectorFactory.emptyInjectorFactory();

    public MapMateBuilder(final PackageScanner packageScanner) {
        this.packageScanner = packageScanner;
    }

    public static MapMateBuilder mapMateBuilder(final String... packageNames) {
        final List<String> packageNameList = Optional.ofNullable(packageNames)
                .map(Arrays::asList)
                .orElse(new LinkedList<>());

        if (packageNameList.isEmpty()) {
            return mapMateBuilder(List::of);
        } else {
            final PackageScanner packageScanner = defaultPackageScanner(packageNameList);
            return mapMateBuilder(packageScanner);
        }
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

    public MapMateBuilder usingMarshaller(final MarshallingType marshallingType,
                                          final Marshaller marshaller,
                                          final Unmarshaller unmarshaller) {
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

    public MapMateBuilder usingInjectorFactory(final InjectorLambda factory) {
        this.injectorFactory = injectorFactory(factory);
        return this;
    }

    public MapMateBuilder usingRecipe(final Recipe recipe) {
        recipe.cook(this);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> MapMateBuilder withExceptionIndicatingValidationError(
            final Class<T> exceptionIndicatingValidationError) {
        return this.withExceptionIndicatingValidationError(
                exceptionIndicatingValidationError,
                (exception, propertyPath) -> new ValidationError(exception.getMessage(), propertyPath));
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> MapMateBuilder withExceptionIndicatingValidationError(
            final Class<T> exceptionIndicatingValidationError,
            final ExceptionMappingWithPropertyPath<T> exceptionMapping) {
        this.validationMappings.putOneToOne(exceptionIndicatingValidationError,
                (ExceptionMappingWithPropertyPath<Throwable>) exceptionMapping);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Throwable> MapMateBuilder withExceptionIndicatingMultipleValidationErrors(
            final Class<T> exceptionType,
            final ExceptionMappingList<T> mapping) {
        validateNotNull(exceptionType, "exceptionType");
        validateNotNull(mapping, "mapping");
        this.validationMappings.putOneToMany(exceptionType, (ExceptionMappingList<Throwable>) mapping);
        return this;
    }

    public MapMateBuilder withCustomPrimitive(final CustomPrimitiveDefinition customPrimitive) {
        final CustomPrimitiveDefinition alreadyAdded = this.manuallyAddedCustomPrimitives.put(
                customPrimitive.type,
                customPrimitive);
        if (alreadyAdded != null) {
            throw new UnsupportedOperationException(String.format(
                    "The customPrimitive %s has already been added for type %s and is %s",
                    customPrimitive,
                    customPrimitive.type,
                    alreadyAdded));
        }

        return this;
    }

    public <T> MapMateBuilder withCustomPrimitive(final Class<T> type,
                                                  final Function<T, String> serializationMethod,
                                                  final Function<String, T> deserializationMethod) {
        return this.withCustomPrimitive(customPrimitiveDefinition(
                type,
                providedMethodSerializationCPMethod(type, serializationMethod),
                StaticMethodCPMethod.theStaticMethodCPMethod(deserializationMethod)
        ));
    }

    public MapMateBuilder withCustomPrimitive(final Class<?> type,
                                              final Method serializationMethod,
                                              final Method deserializationMethod) {
        return this.withCustomPrimitive(customPrimitiveDefinition(type, serializationMethod, deserializationMethod));
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

    public MapMateBuilder withSerializedObject(final Class<?> type,
                                               final Field[] serializedFields,
                                               final String deserializationMethodName) {
        return this.withSerializedObject(serializedObjectDefinition(type, serializedFields, deserializationMethodName));
    }

    public MapMateBuilder withSerializedObjects(final Class<?>... serializedObjectTypes) {
        this.manuallyAddedSerializedObjectTypes.addAll(Arrays.asList(serializedObjectTypes));
        return this;
    }

    public MapMateBuilder withCustomPrimitives(final Class<?>... customPrimitiveTypes) {
        this.manuallyAddedCustomPrimitiveTypes.addAll(Arrays.asList(customPrimitiveTypes));
        return this;
    }

    public MapMate build() {
        final List<Class<?>> scannedClasses = this.packageScanner.scan();
        final List<Class<?>> withoutManualClasses = scannedClasses.stream()
                .filter(aClass -> !this.manuallyAddedCustomPrimitives.containsKey(aClass) &&
                        !this.manuallyAddedSerializedObjects.containsKey(aClass))
                .collect(Collectors.toList());
        final List<CustomPrimitiveDefinition> cpDefinitions = this.detector.customPrimitives(withoutManualClasses);
        final List<Class<?>> withoutCP = withoutManualClasses.stream()
                .filter(aClass -> cpDefinitions.stream().noneMatch(d -> d.type == aClass))
                .collect(Collectors.toList());
        final List<SerializedObjectDefinition> serializedDTOs = this.detector.serializedObjects(withoutCP);
        this.addCustomPrimitives(cpDefinitions);
        this.addCustomPrimitives(this.manuallyAddedCustomPrimitives.values());
        this.addCustomPrimitives(this.detector.customPrimitives(this.manuallyAddedCustomPrimitiveTypes));
        this.addSerializedObjects(serializedDTOs);
        this.addSerializedObjects(this.manuallyAddedSerializedObjects.values());
        this.addSerializedObjects(this.detector.serializedObjects(this.manuallyAddedSerializedObjectTypes));
        final SerializableDefinitions serializables = serializableDefinitions(
                this.serializableCPs,
                this.serializableDTOs
        );
        final DeserializableDefinitions deserializables = deserializableDefinitions(
                this.deserializableCPs,
                this.deserializableDTOs
        );
        final MarshallerRegistry<Marshaller> marshallerRegistry = marshallerRegistry(this.marshallerMap);
        final MarshallerRegistry<Unmarshaller> unmarshallerRegistry = marshallerRegistry(this.unmarshallerMap);

        final Serializer serializer = theSerializer(marshallerRegistry, serializables);
        final Deserializer deserializer = theDeserializer(unmarshallerRegistry, deserializables, this.validationMappings,
                this.validationErrorsMapping, false, this.injectorFactory);
        return mapMate(serializer, deserializer);
    }

    private void addCustomPrimitives(final Iterable<CustomPrimitiveDefinition> customPrimitiveDefinitions) {
        for (final CustomPrimitiveDefinition customPrimitiveDefinition : customPrimitiveDefinitions) {
            final Class<?> type = customPrimitiveDefinition.type;
            final DeserializationCPMethod deserializer = customPrimitiveDefinition.deserializer;
            final SerializationCPMethod serializer = customPrimitiveDefinition.serializer;

            final DeserializableCustomPrimitive<?> deserializableCP = deserializableCustomPrimitive(type, deserializer);
            this.deserializableCPs.add(deserializableCP);

            final SerializableCustomPrimitive serializableCP = serializableCustomPrimitive(type, serializer);
            this.serializableCPs.add(serializableCP);
        }
    }

    private void addSerializedObjects(final Iterable<SerializedObjectDefinition> values) {
        for (final SerializedObjectDefinition serializedObjectDefinition : values) {
            final DeserializationDTOMethod deserializer = serializedObjectDefinition.deserializer;
            final SerializationDTOMethod serializer = serializedObjectDefinition.serializer;
            final Class<?> type = serializedObjectDefinition.type;

            if (deserializer != null) {
                final DeserializableDataTransferObject<?> deserializableDTO = deserializableDataTransferObject(
                        type,
                        deserializer
                );
                this.deserializableDTOs.add(deserializableDTO);
            }
            if (serializer != null) {
                final SerializableDataTransferObject serializableDTO = serializableDataTransferObject(type, serializer);
                this.serializableDTOs.add(serializableDTO);
            }
        }
    }
}

