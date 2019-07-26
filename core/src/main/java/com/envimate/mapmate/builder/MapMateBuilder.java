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

import com.envimate.mapmate.builder.anticorruption.DefinitionsFactory;
import com.envimate.mapmate.builder.conventional.ConventionalDetector;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.Unmarshaller;
import com.envimate.mapmate.deserialization.validation.*;
import com.envimate.mapmate.injector.InjectorFactory;
import com.envimate.mapmate.injector.InjectorLambda;
import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.Marshaller;
import com.envimate.mapmate.serialization.Serializer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.envimate.mapmate.builder.DefaultPackageScanner.defaultPackageScanner;
import static com.envimate.mapmate.builder.MapMate.mapMate;
import static com.envimate.mapmate.builder.anticorruption.DefinitionsFactory.definitionsFactory;
import static com.envimate.mapmate.deserialization.Deserializer.theDeserializer;
import static com.envimate.mapmate.injector.InjectorFactory.injectorFactory;
import static com.envimate.mapmate.marshalling.MarshallerRegistry.marshallerRegistry;
import static com.envimate.mapmate.serialization.Serializer.theSerializer;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

public final class MapMateBuilder {
    private static final int INITIAL_CAPACITY = 10000;
    public Detector detector = ConventionalDetector.conventionalDetectorWithAnnotations();
    private final PackageScanner packageScanner;
    private final List<Recipe> recipes = new LinkedList<>();
    private final ValidationMappings validationMappings = ValidationMappings.empty();
    private final ValidationErrorsMapping validationErrorsMapping = validationErrors -> {
        throw AggregatedValidationException.fromList(validationErrors);
    };
    private Map<MarshallingType, Marshaller> marshallerMap = new HashMap<>(1);
    private Map<MarshallingType, Unmarshaller> unmarshallerMap = new HashMap<>(1);
    private InjectorFactory injectorFactory = InjectorFactory.emptyInjectorFactory();

    private MapMateBuilder(final PackageScanner packageScanner) {
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

    public MapMateBuilder usingJsonMarshaller(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "jsonMarshaller");
        validateNotNull(unmarshaller, "jsonUnmarshaller");
        this.marshallerMap.put(MarshallingType.json(), marshaller);
        this.unmarshallerMap.put(MarshallingType.json(), unmarshaller);
        return this;
    }

    public MapMateBuilder usingYamlMarshaller(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "yamlMarshaller");
        validateNotNull(unmarshaller, "yamlUnmarshaller");
        this.marshallerMap.put(MarshallingType.yaml(), marshaller);
        this.unmarshallerMap.put(MarshallingType.yaml(), unmarshaller);
        return this;
    }

    public MapMateBuilder usingXmlMarshaller(final Marshaller marshaller, final Unmarshaller unmarshaller) {
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

    public MapMateBuilder usingMarshaller(final Map<MarshallingType, Marshaller> marshallerMap,
                                          final Map<MarshallingType, Unmarshaller> unmarshallerMap) {
        this.marshallerMap = new HashMap<>(marshallerMap);
        this.unmarshallerMap = new HashMap<>(unmarshallerMap);
        return this;
    }

    public MapMateBuilder usingInjectorFactory(final InjectorLambda factory) {
        this.injectorFactory = injectorFactory(factory);
        return this;
    }

    public MapMateBuilder usingRecipe(final Recipe recipe) {
        this.recipes.add(recipe);
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

    public MapMate build() {
        this.recipes.forEach(recipe -> recipe.cook(this));

        final Map<Class<?>, CustomPrimitiveDefinition> customPrimitives = new HashMap<>(INITIAL_CAPACITY);
        this.recipes.stream()
                .map(Recipe::customPrimitiveDefinitions)
                .forEach(customPrimitives::putAll);
        final Map<Class<?>, SerializedObjectDefinition> serializedObjects = new HashMap<>(INITIAL_CAPACITY);
        this.recipes.stream()
                .map(Recipe::serializedObjectDefinitions)
                .forEach(serializedObjects::putAll);
        final List<Class<?>> scannedClasses = this.packageScanner.scan();
        final List<Class<?>> customPrimitiveDetectionCandidates = scannedClasses.stream()
                .filter(detectionCandidate ->
                        !customPrimitives.containsKey(detectionCandidate) &&
                                !serializedObjects.containsKey(detectionCandidate)
                )
                .collect(Collectors.toList());
        final Map<Class<?>, CustomPrimitiveDefinition> detectedCustomPrimitives = this.detector
                .customPrimitives(customPrimitiveDetectionCandidates)
                .stream()
                .collect(Collectors.toMap(o -> o.type, Function.identity()));
        customPrimitives.putAll(detectedCustomPrimitives);

        final List<Class<?>> serializedObjectDetectionCandidates = customPrimitiveDetectionCandidates.stream()
                .filter(detectionCandidate -> !detectedCustomPrimitives.containsKey(detectionCandidate))
                .collect(Collectors.toList());
        final Map<Class<?>, SerializedObjectDefinition> detectedSerializedObjects = this.detector
                .serializedObjects(serializedObjectDetectionCandidates)
                .stream()
                .collect(Collectors.toMap(o -> o.type, Function.identity()));
        serializedObjects.putAll(detectedSerializedObjects);

        final MarshallerRegistry<Marshaller> marshallerRegistry = marshallerRegistry(this.marshallerMap);
        final DefinitionsFactory definitionsFactory = definitionsFactory(
                customPrimitives.values(),
                serializedObjects.values()
        );
        final Serializer serializer = theSerializer(marshallerRegistry, definitionsFactory.toSerializableDefinitions());

        final MarshallerRegistry<Unmarshaller> unmarshallerRegistry = marshallerRegistry(this.unmarshallerMap);
        final Deserializer deserializer = theDeserializer(
                unmarshallerRegistry,
                definitionsFactory.toDeserializableDefinitions(),
                this.validationMappings,
                this.validationErrorsMapping,
                false,
                this.injectorFactory
        );
        return mapMate(serializer, deserializer);
    }
}

