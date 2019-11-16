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

package com.envimate.mapmate.builder.usecases;

import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinition;
import com.envimate.mapmate.builder.definitions.CustomPrimitiveDefinitionFactory;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.builder.definitions.SerializedObjectDefinitionFactory;
import com.envimate.mapmate.serialization.methods.SerializationField;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.envimate.mapmate.builder.detection.customprimitive.SimpleCustomPrimitiveDefinitionFactory.definitionFactory;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.StaticMethodBasedCustomPrimitiveDeserializationDetector.staticMethodBased;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.MethodNameBasedCustomPrimitiveSerializationDetector.methodNameBased;
import static com.envimate.mapmate.builder.detection.serializedobject.SimpleSerializedObjectDefinitionFactory.serializedObjectFactory;
import static com.envimate.mapmate.builder.detection.serializedobject.detectors.SingleMethodDeserializationDetector.singleMethodBased;
import static com.envimate.mapmate.builder.usecases.ScanResultBuilder.scanResultBuilder;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCasesDetector {
    private final CustomPrimitiveDefinitionFactory customPrimitiveFactory = definitionFactory(methodNameBased("stringValue"), staticMethodBased());
    private final SerializedObjectDefinitionFactory serializedObjectFactory = serializedObjectFactory(singleMethodBased());

    public static UseCasesDetector useCasesDetector() {
        return new UseCasesDetector();
    }

    public void detectFromUseCase(final Class<?> useCase) {
        findUseCaseMethod(useCase).ifPresent(method -> {
            final ScanResultBuilder scanResultBuilder = scanResultBuilder();
            returnTypeOf(method).ifPresent(returnType -> scanClassRecursively(returnType, scanResultBuilder));
        });
    }

    private Optional<Class<?>> returnTypeOf(final Method method) {
        final Class<?> returnType = method.getReturnType();
        if (Void.TYPE.equals(returnType)) {
            return empty();
        } else {
            return of(returnType);
        }
    }

    private Optional<Method> findUseCaseMethod(final Class<?> useCase) {
        final List<Method> relevantMethods = stream(useCase.getMethods())
                .filter(method -> !isStatic(method.getModifiers()))
                .filter(method -> isPublic(method.getModifiers()))
                .collect(toList());
        if (relevantMethods.size() != 1) {
            throw new UnsupportedOperationException(format("Type '%s' is not a usecase", useCase.getName()));
        }
        return of(relevantMethods.get(0));
    }

    private void scanClassRecursively(final Class<?> type,
                                      final ScanResultBuilder scanResultBuilder) {
        final Optional<CustomPrimitiveDefinition> maybeCustomPrimitive = this.customPrimitiveFactory.analyze(type);
        maybeCustomPrimitive.ifPresent(scanResultBuilder::addCustomPrimitive);

        if (maybeCustomPrimitive.isPresent()) {
            return;
        }

        final Optional<SerializedObjectDefinition> maybeSerializedObject = this.serializedObjectFactory.analyze(type);
        maybeSerializedObject.ifPresent(serializedObject -> {
            final List<SerializationField> fields = serializedObject.serializer.fields();
            fields.forEach(field -> scanClassRecursively(field.type(), scanResultBuilder));
            scanResultBuilder.addSerializedObject(serializedObject);
        });

        if (maybeSerializedObject.isEmpty()) {
            throw new RuntimeException(format("Unable not load type '%s'", type.getName()));
        }
    }
}
