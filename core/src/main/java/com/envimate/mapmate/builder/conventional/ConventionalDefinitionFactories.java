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

package com.envimate.mapmate.builder.conventional;

import com.envimate.mapmate.builder.conventional.annotations.*;
import com.envimate.mapmate.builder.detection.DefinitionFactory;
import com.envimate.mapmate.builder.detection.customprimitive.deserialization.CustomPrimitiveDeserializationDetector;
import com.envimate.mapmate.builder.detection.customprimitive.serialization.CustomPrimitiveSerializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.ClassFilter;
import com.envimate.mapmate.builder.detection.serializedobject.deserialization.ConstructorBasedDeserializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.deserialization.SerializedObjectDeserializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.fields.FieldDetector;

import java.util.List;
import java.util.regex.Pattern;

import static com.envimate.mapmate.builder.detection.customprimitive.CustomPrimitiveDefinitionFactory.customPrimitiveFactory;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.ClassAnnotationBasedCustomPrimitiveDeserializationDetector.classAnnotationBasedDeserializer;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.ConstructorBasedCustomPrimitiveDeserializationDetector.constructorBased;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.MethodAnnotationBasedCustomPrimitiveDeserializationDetector.annotationBasedDeserializer;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.StaticMethodBasedCustomPrimitiveDeserializationDetector.staticMethodBased;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.ClassAnnotationBasedCustomPrimitiveSerializationDetector.classAnnotationBasedSerializer;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.MethodAnnotationBasedCustomPrimitiveSerializationDetector.annotationBasedSerializer;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.MethodNameBasedCustomPrimitiveSerializationDetector.methodNameBased;
import static com.envimate.mapmate.builder.detection.serializedobject.ClassFilter.allowAll;
import static com.envimate.mapmate.builder.detection.serializedobject.ClassFilter.patternFilter;
import static com.envimate.mapmate.builder.detection.serializedobject.SerializedObjectDefinitionFactory.serializedObjectFactory;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.AnnotationBasedDeserializationDetector.annotationBasedDeserialzer;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.MatchingMethodDeserializationDetector.matchingMethodBased;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.NamedMethodDeserializationDetector.namedMethodBased;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.SingleMethodDeserializationDetector.singleMethodBased;
import static com.envimate.mapmate.builder.detection.serializedobject.fields.AnnotationFieldDetector.annotationBased;

public final class ConventionalDefinitionFactories {

    private ConventionalDefinitionFactories() {
    }

    public static DefinitionFactory nameAndConstructorBasedCustomPrimitiveDefinitionFactory(
            final String serializationMethodName,
            final String deserializationMethodName) {
        return customPrimitiveFactory(methodNameBased(serializationMethodName),
                staticMethodBased(deserializationMethodName),
                constructorBased());
    }

    public static DefinitionFactory nameBasedCustomPrimitiveDefinitionFactory(
            final String serializationMethodName,
            final String deserializationMethodName) {
        return customPrimitiveFactory(methodNameBased(serializationMethodName),
                staticMethodBased(deserializationMethodName));
    }

    public static DefinitionFactory customPrimitiveMethodAnnotationFactory() {
        final CustomPrimitiveSerializationDetector serializationDetector = annotationBasedSerializer(
                MapMatePrimitiveSerializer.class);
        final CustomPrimitiveDeserializationDetector deserializationDetector = annotationBasedDeserializer(
                MapMatePrimitiveDeserializer.class);
        return customPrimitiveFactory(serializationDetector, deserializationDetector);
    }

    public static DefinitionFactory customPrimitiveClassAnnotationFactory() {
        final CustomPrimitiveSerializationDetector serializationDetector =
                classAnnotationBasedSerializer(MapMatePrimitive.class, MapMatePrimitive::serializationMethodName);
        final CustomPrimitiveDeserializationDetector deserializationDetector =
                classAnnotationBasedDeserializer(MapMatePrimitive.class, MapMatePrimitive::deserializationMethodName);
        return customPrimitiveFactory(serializationDetector, deserializationDetector);
    }

    public static DefinitionFactory nameAndConstructorBasedSerializedObjectFactory(
            final List<Pattern> patterns,
            final String deserializationMethodNamePattern) {
        final ClassFilter filter = patternFilter(patterns);
        final SerializedObjectDeserializationDetector singleMethod = singleMethodBased();
        final SerializedObjectDeserializationDetector matchingMethod =
                matchingMethodBased(deserializationMethodNamePattern);
        final SerializedObjectDeserializationDetector constructor =
                ConstructorBasedDeserializationDetector.constructorBased();
        return serializedObjectFactory(filter, singleMethod, matchingMethod, constructor);
    }

    public static DefinitionFactory nameBasedSerializedObjectFactory(
            final List<Pattern> patterns,
            final String deserializationMethodNamePattern) {
        final ClassFilter filter = patternFilter(patterns);
        final SerializedObjectDeserializationDetector singleMethod = singleMethodBased();
        final SerializedObjectDeserializationDetector matchingMethod =
                matchingMethodBased(deserializationMethodNamePattern);
        return serializedObjectFactory(filter, singleMethod, matchingMethod);
    }

    public static DefinitionFactory deserializerMethodNameBasedSerializedObjectFactory(
            final String deserializationMethodName) {
        return serializedObjectFactory(namedMethodBased(deserializationMethodName));
    }

    public static DefinitionFactory serializedObjectClassAnnotationFactory() {
        final FieldDetector fieldDetector = annotationBased(MapMateSerializedField.class);
        final SerializedObjectDeserializationDetector deserializationDetector =
                annotationBasedDeserialzer(MapMateDeserializationMethod.class);
        return serializedObjectFactory(allowAll(), fieldDetector, deserializationDetector);
    }
}
