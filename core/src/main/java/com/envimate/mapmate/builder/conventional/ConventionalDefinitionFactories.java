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
import com.envimate.mapmate.builder.detection.customprimitive.mapping.CustomPrimitiveMappings;
import com.envimate.mapmate.builder.detection.customprimitive.serialization.CustomPrimitiveSerializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.deserialization.ConstructorBasedDeserializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.deserialization.SerializedObjectDeserializationDetector;
import com.envimate.mapmate.builder.detection.serializedobject.fields.FieldDetector;
import com.envimate.mapmate.definitions.universal.UniversalBoolean;
import com.envimate.mapmate.definitions.universal.UniversalNumber;
import com.envimate.mapmate.definitions.universal.UniversalString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.builder.detection.customprimitive.CustomPrimitiveDefinitionFactory.customPrimitiveFactory;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.ClassAnnotationBasedCustomPrimitiveDeserializationDetector.classAnnotationBasedDeserializer;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.ConstructorBasedCustomPrimitiveDeserializationDetector.constructorBased;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.MethodAnnotationBasedCustomPrimitiveDeserializationDetector.annotationBasedDeserializer;
import static com.envimate.mapmate.builder.detection.customprimitive.deserialization.StaticMethodBasedCustomPrimitiveDeserializationDetector.staticMethodBased;
import static com.envimate.mapmate.builder.detection.customprimitive.mapping.CustomPrimitiveMappings.customPrimitiveMappings;
import static com.envimate.mapmate.builder.detection.customprimitive.mapping.UniversalTypeMapper.universalTypeMapper;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.ClassAnnotationBasedCustomPrimitiveSerializationDetector.classAnnotationBasedSerializer;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.MethodAnnotationBasedCustomPrimitiveSerializationDetector.annotationBasedSerializer;
import static com.envimate.mapmate.builder.detection.customprimitive.serialization.MethodNameBasedCustomPrimitiveSerializationDetector.methodNameBased;
import static com.envimate.mapmate.builder.detection.serializedobject.ClassFilter.allowAll;
import static com.envimate.mapmate.builder.detection.serializedobject.SerializedObjectDefinitionFactory.serializedObjectFactory;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.AnnotationBasedDeserializationDetector.annotationBasedDeserialzer;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.MatchingMethodDeserializationDetector.matchingMethodBased;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.NamedMethodDeserializationDetector.namedMethodBased;
import static com.envimate.mapmate.builder.detection.serializedobject.deserialization.SingleMethodDeserializationDetector.singleMethodBased;
import static com.envimate.mapmate.builder.detection.serializedobject.fields.AnnotationFieldDetector.annotationBased;
import static com.envimate.mapmate.builder.detection.serializedobject.fields.ModifierFieldDetector.modifierBased;
import static com.envimate.mapmate.definitions.universal.UniversalNumber.universalNumber;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class ConventionalDefinitionFactories {

    public static final CustomPrimitiveMappings CUSTOM_PRIMITIVE_MAPPINGS = customPrimitiveMappings(
            universalTypeMapper(String.class, UniversalString.class),
            universalTypeMapper(double.class, UniversalNumber.class),
            universalTypeMapper(Double.class, UniversalNumber.class),
            universalTypeMapper(boolean.class, UniversalBoolean.class),
            universalTypeMapper(Boolean.class, UniversalBoolean.class),
            universalTypeMapper(int.class, UniversalNumber.class,
                    integer -> universalNumber(Double.valueOf(integer)),
                    universalNumber -> ((Double) universalNumber.toNativeJava()).intValue()),
            universalTypeMapper(Integer.class, UniversalNumber.class,
                    integer -> universalNumber(Double.valueOf(integer)),
                    universalNumber -> ((Double) universalNumber.toNativeJava()).intValue())
    );

    private ConventionalDefinitionFactories() {
    }

    public static DefinitionFactory nameAndConstructorBasedCustomPrimitiveDefinitionFactory(
            final String serializationMethodName,
            final String deserializationMethodName) {
        return customPrimitiveFactory(
                methodNameBased(CUSTOM_PRIMITIVE_MAPPINGS, serializationMethodName),
                staticMethodBased(CUSTOM_PRIMITIVE_MAPPINGS, deserializationMethodName),
                constructorBased(CUSTOM_PRIMITIVE_MAPPINGS)
        );
    }

    public static DefinitionFactory customPrimitiveMethodAnnotationFactory() {
        final CustomPrimitiveSerializationDetector serializationDetector = annotationBasedSerializer(MapMatePrimitiveSerializer.class);
        final CustomPrimitiveDeserializationDetector deserializationDetector = annotationBasedDeserializer(MapMatePrimitiveDeserializer.class);
        return customPrimitiveFactory(serializationDetector, deserializationDetector);
    }

    public static DefinitionFactory customPrimitiveClassAnnotationFactory() {
        final CustomPrimitiveSerializationDetector serializationDetector =
                classAnnotationBasedSerializer(MapMatePrimitive.class, MapMatePrimitive::serializationMethodName);
        final CustomPrimitiveDeserializationDetector deserializationDetector =
                classAnnotationBasedDeserializer(MapMatePrimitive.class, MapMatePrimitive::deserializationMethodName);
        return customPrimitiveFactory(serializationDetector, deserializationDetector);
    }

    public static DefinitionFactory allSerializedObjectFactory(final String deserializationMethodName) {
        final List<FieldDetector> fieldDetectors = new LinkedList<>();
        fieldDetectors.add(annotationBased(MapMateSerializedField.class));
        fieldDetectors.add(modifierBased());

        final SerializedObjectDeserializationDetector namedMethodBased = namedMethodBased(deserializationMethodName);
        final SerializedObjectDeserializationDetector singleMethod = singleMethodBased();
        final SerializedObjectDeserializationDetector matchingMethod = matchingMethodBased(deserializationMethodName);
        final SerializedObjectDeserializationDetector constructor = ConstructorBasedDeserializationDetector.constructorBased();
        final List<SerializedObjectDeserializationDetector> deserializationDetectors =
                asList(namedMethodBased, singleMethod, matchingMethod, constructor);

        return serializedObjectFactory(allowAll(), fieldDetectors, deserializationDetectors);
    }

    public static DefinitionFactory serializedObjectClassAnnotationFactory() {
        final FieldDetector fieldDetector = annotationBased(MapMateSerializedField.class);
        final SerializedObjectDeserializationDetector deserializationDetector = annotationBasedDeserialzer(MapMateDeserializationMethod.class);
        return serializedObjectFactory(allowAll(), asList(fieldDetector), singletonList(deserializationDetector));
    }
}
