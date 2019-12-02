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

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.Definitions;
import com.envimate.mapmate.mapper.deserialization.Deserializer;
import com.envimate.mapmate.mapper.deserialization.validation.*;
import com.envimate.mapmate.mapper.injector.InjectorFactory;
import com.envimate.mapmate.mapper.injector.InjectorLambda;
import com.envimate.mapmate.mapper.marshalling.Marshaller;
import com.envimate.mapmate.mapper.marshalling.MarshallerRegistry;
import com.envimate.mapmate.mapper.marshalling.MarshallingType;
import com.envimate.mapmate.mapper.marshalling.Unmarshaller;
import com.envimate.mapmate.mapper.serialization.Serializer;
import com.envimate.mapmate.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.builder.conventional.ConventionalDetectors;
import com.envimate.mapmate.builder.conventional.DetectorBuilder;
import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.builder.scanning.PackageScanner;
import com.envimate.mapmate.shared.types.ResolvedType;

import java.util.*;

import static com.envimate.mapmate.MapMate.mapMate;
import static com.envimate.mapmate.mapper.deserialization.Deserializer.theDeserializer;
import static com.envimate.mapmate.mapper.injector.InjectorFactory.injectorFactory;
import static com.envimate.mapmate.mapper.marshalling.MarshallerRegistry.marshallerRegistry;
import static com.envimate.mapmate.mapper.serialization.Serializer.theSerializer;
import static com.envimate.mapmate.builder.DefinitionsBuilder.definitionsBuilder;
import static com.envimate.mapmate.builder.DependencyRegistry.dependency;
import static com.envimate.mapmate.builder.DependencyRegistry.dependencyRegistry;
import static com.envimate.mapmate.builder.RequiredCapabilities.all;
import static com.envimate.mapmate.builder.contextlog.BuildContextLog.emptyLog;
import static com.envimate.mapmate.builder.conventional.ConventionalDefinitionFactories.CUSTOM_PRIMITIVE_MAPPINGS;
import static com.envimate.mapmate.builder.scanning.DefaultPackageScanner.defaultPackageScanner;
import static com.envimate.mapmate.builder.scanning.PackageScannerRecipe.packageScannerRecipe;
import static com.envimate.mapmate.shared.types.ClassType.fromClassWithoutGenerics;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;
import static java.util.Arrays.stream;

public final class MapMateBuilder {
    private final BuildContextLog contextLog = emptyLog();
    private final DependencyRegistry dependencyRegistry = dependencyRegistry(
            dependency(Detector.class, () -> this.detector)
    );
    private final List<Definition> addedDefinitions = new LinkedList<>();
    private final List<Recipe> recipes = new LinkedList<>();
    private final ValidationMappings validationMappings = ValidationMappings.empty();
    private final ValidationErrorsMapping validationErrorsMapping = validationErrors -> {
        throw AggregatedValidationException.fromList(validationErrors);
    };
    private Map<MarshallingType, Marshaller> marshallerMap = new HashMap<>(1);
    private Map<MarshallingType, Unmarshaller> unmarshallerMap = new HashMap<>(1);
    private volatile InjectorFactory injectorFactory = InjectorFactory.emptyInjectorFactory();
    private Detector detector = ConventionalDetectors.conventionalDetector();

    public static MapMateBuilder mapMateBuilder(final String... packageNames) {
        if (packageNames != null) {
            stream(packageNames).forEach(packageName -> validateNotNull(packageName, "packageName"));
        }
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
        return new MapMateBuilder().usingRecipe(packageScannerRecipe(packageScanner));
    }

    public MapMateBuilder withDetector(final DetectorBuilder detector) {
        return withDetector(detector.build());
    }

    public MapMateBuilder withDetector(final Detector detector) {
        this.detector = detector;
        return this;
    }


    public MapMateBuilder withManuallyAddedType(final Class<?> type) {
        return withManuallyAddedType(fromClassWithoutGenerics(type), this.contextLog);
    }

    public MapMateBuilder withManuallyAddedType(final Class<?> type,
                                                final RequiredCapabilities capabilities) {
        return withManuallyAddedType(fromClassWithoutGenerics(type), capabilities);
    }

    public MapMateBuilder withManuallyAddedType(final ResolvedType type,
                                                final RequiredCapabilities capabilities) {
        validateNotNull(type, "type");
        validateNotNull(capabilities, "capabilities");
        final Definition definition = this.detector.detect(type, capabilities, this.contextLog).orElseThrow();
        return withManuallyAddedDefinition(definition);
    }

    public MapMateBuilder withManuallyAddedType(final ResolvedType type, final BuildContextLog contextLog) {
        validateNotNull(type, "type");
        contextLog.stepInto(MapMateBuilder.class).log(type, "added");
        return withManuallyAddedType(type);
    }

    public MapMateBuilder withManuallyAddedType(final ResolvedType type) {
        return withManuallyAddedType(type, all());
    }

    public MapMateBuilder withManuallyAddedTypes(final Class<?>... type) {
        validateNotNull(type, "type");
        stream(type).forEach(this::withManuallyAddedType);
        return this;
    }

    public MapMateBuilder withManuallyAddedDefinition(final Definition definition) {
        validateNotNull(definition, "definition");
        this.addedDefinitions.add(definition);
        return this;
    }

    public MapMateBuilder usingJsonMarshaller(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "jsonMarshaller");
        validateNotNull(unmarshaller, "jsonUnmarshaller");
        return usingMarshaller(MarshallingType.json(), marshaller, unmarshaller);
    }

    public MapMateBuilder usingYamlMarshaller(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "yamlMarshaller");
        validateNotNull(unmarshaller, "yamlUnmarshaller");
        return usingMarshaller(MarshallingType.yaml(), marshaller, unmarshaller);
    }

    public MapMateBuilder usingXmlMarshaller(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        validateNotNull(marshaller, "xmlMarshaller");
        validateNotNull(unmarshaller, "xmlUnmarshaller");
        return usingMarshaller(MarshallingType.xml(), marshaller, unmarshaller);
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

    public BuildContextLog contextLog() {
        return this.contextLog;
    }

    public MapMate build() {
        this.recipes.forEach(recipe -> recipe.init(this.dependencyRegistry));

        this.recipes.forEach(recipe -> {
            recipe.cook(this, this.dependencyRegistry);
        });

        final DefinitionsBuilder definitionsBuilder = definitionsBuilder(this.detector, this.contextLog);
        this.addedDefinitions.forEach(definitionsBuilder::addDefinition);

        definitionsBuilder.resolveRecursively(this.detector);
        final Definitions definitions = definitionsBuilder.build();

        final MarshallerRegistry<Marshaller> marshallerRegistry = marshallerRegistry(this.marshallerMap);
        final Serializer serializer = theSerializer(marshallerRegistry, definitions, CUSTOM_PRIMITIVE_MAPPINGS);

        final MarshallerRegistry<Unmarshaller> unmarshallerRegistry = marshallerRegistry(this.unmarshallerMap);
        final Deserializer deserializer = theDeserializer(
                unmarshallerRegistry,
                definitions,
                CUSTOM_PRIMITIVE_MAPPINGS,
                this.validationMappings,
                this.validationErrorsMapping,
                this.injectorFactory
        );
        return mapMate(serializer, deserializer);
    }
}

