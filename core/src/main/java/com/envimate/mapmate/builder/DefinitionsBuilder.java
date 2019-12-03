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

import com.envimate.mapmate.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.builder.detection.Detector;
import com.envimate.mapmate.mapper.definitions.CollectionDefinition;
import com.envimate.mapmate.mapper.definitions.Definition;
import com.envimate.mapmate.mapper.definitions.Definitions;
import com.envimate.mapmate.mapper.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.mapper.deserialization.deserializers.TypeDeserializer;
import com.envimate.mapmate.mapper.deserialization.deserializers.collections.CollectionDeserializer;
import com.envimate.mapmate.mapper.deserialization.deserializers.customprimitives.CustomPrimitiveDeserializer;
import com.envimate.mapmate.mapper.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.mapper.serialization.serializers.TypeSerializer;
import com.envimate.mapmate.mapper.serialization.serializers.collections.CollectionSerializer;
import com.envimate.mapmate.mapper.serialization.serializers.customprimitives.CustomPrimitiveSerializer;
import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializedObjectSerializer;
import com.envimate.mapmate.shared.types.ClassType;
import com.envimate.mapmate.shared.types.ResolvedType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.envimate.mapmate.builder.RequiredCapabilities.deserializationOnly;
import static com.envimate.mapmate.builder.RequiredCapabilities.serializationOnly;
import static com.envimate.mapmate.mapper.definitions.CustomPrimitiveDefinition.untypedCustomPrimitiveDefinition;
import static com.envimate.mapmate.mapper.definitions.Definitions.definitions;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionsBuilder {
    private static final int INITIAL_CAPACITY = 10000;

    private final DefinitionsBuilder2<TypeDeserializer> deserializers = DefinitionsBuilder2.definitionsBuilder("deserializer");
    private final DefinitionsBuilder2<TypeSerializer> serializers = DefinitionsBuilder2.definitionsBuilder("serializer");

    private final BuildContextLog contextLog;
    private final Detector detector;

    public static DefinitionsBuilder definitionsBuilder(final Detector detector,
                                                        final BuildContextLog contextLog) {
        return new DefinitionsBuilder(contextLog.stepInto(DefinitionsBuilder.class), detector);
    }

    public void addSerializer(final ResolvedType type,
                              final TypeSerializer serializer) {
        this.serializers.add(type, serializer);
    }

    public void addDeserializer(final ResolvedType type,
                                final TypeDeserializer deserializer) {
        this.deserializers.add(type, deserializer);
    }

    public void resolveRecursively(final Detector detector) {
        this.serializers.values().forEach(serializer -> diveIntoSerializerChildren(serializer, detector, this.contextLog));
        this.deserializers.values().forEach(deserializer -> diveIntoDeserializerChildren(deserializer, detector, this.contextLog));
    }

    private void recurseDeserializers(final ResolvedType type,
                                      final Detector detector,
                                      final BuildContextLog contextLog) {
        if (this.deserializers.isPresent(type)) {
            return;
        }
        detector.detect(type, deserializationOnly(), contextLog).ifPresent(definition -> {
            contextLog.log(type, "added because it is a dependency");
            definition.deserializer().ifPresent(deserializer -> {
                this.deserializers.add(type, deserializer);
                diveIntoDeserializerChildren(deserializer, detector, contextLog);
            });
        });
    }

    private void diveIntoDeserializerChildren(final TypeDeserializer deserializer,
                                              final Detector detector,
                                              final BuildContextLog contextLog) {
        deserializer.requiredTypes().forEach(requiredType -> recurseDeserializers(requiredType, detector, contextLog.stepInto(requiredType.assignableType())));
    }

    private void recurseSerializers(final ResolvedType type,
                                    final Detector detector,
                                    final BuildContextLog contextLog) {
        if (this.serializers.isPresent(type)) {
            return;
        }
        detector.detect(type, serializationOnly(), contextLog).ifPresent(definition -> {
            contextLog.log(type, "added because it is a dependency");
            definition.serializer().ifPresent(serializer -> {
                this.serializers.add(type, serializer);
                diveIntoSerializerChildren(serializer, detector, contextLog);
            });
        });
    }

    private void diveIntoSerializerChildren(final TypeSerializer serializer,
                                            final Detector detector,
                                            final BuildContextLog contextLog) {
        serializer.requiredTypes().forEach(requiredType -> recurseSerializers(requiredType, detector, contextLog.stepInto(requiredType.assignableType())));
    }

    public Definitions build() {
        final Set<ResolvedType> allTypes = new HashSet<>();
        allTypes.addAll(this.deserializers.keys());
        allTypes.addAll(this.serializers.keys());

        final Map<ResolvedType, Definition> definitions = allTypes.stream()
                .map(this::definitionForType)
                .collect(toMap(Definition::type, definition -> definition));
        return definitions(this.contextLog, definitions);
    }

    private Definition definitionForType(final ResolvedType type) {
        final TypeDeserializer deserializer = this.deserializers.get(type).orElse(null);
        final TypeSerializer serializer = this.serializers.get(type).orElse(null);


        if (nullOrInstance(deserializer, CustomPrimitiveDeserializer.class) && nullOrInstance(serializer, CustomPrimitiveSerializer.class)) {
            final CustomPrimitiveSerializer customPrimitiveSerializer = (CustomPrimitiveSerializer) serializer;
            final CustomPrimitiveDeserializer customPrimitiveDeserializer = (CustomPrimitiveDeserializer) deserializer;
            return untypedCustomPrimitiveDefinition(type, customPrimitiveSerializer, customPrimitiveDeserializer);
        }

        if (nullOrInstance(deserializer, SerializedObjectDeserializer.class) && nullOrInstance(serializer, SerializedObjectSerializer.class)) {
            final SerializedObjectSerializer serializedObjectSerializer = (SerializedObjectSerializer) serializer;
            final SerializedObjectDeserializer serializedObjectDeserializer = (SerializedObjectDeserializer) deserializer;
            final ClassType classType = (ClassType) type;
            return SerializedObjectDefinition.serializedObjectDefinition(classType, serializedObjectSerializer, serializedObjectDeserializer);
        }

        if (nullOrInstance(deserializer, CollectionDeserializer.class) && nullOrInstance(serializer, CollectionSerializer.class)) {
            final CollectionSerializer collectionSerializer = (CollectionSerializer) serializer;
            final CollectionDeserializer collectionDeserializer = (CollectionDeserializer) deserializer;
            final ResolvedType contentType = collectionDeserializer.contentType();
            return CollectionDefinition.collectionDefinition(type, contentType, collectionSerializer, collectionDeserializer);
        }

        throw new UnsupportedOperationException("It is not clear what type of definition to assign to " + type.description() +
                " with deserializer " + deserializer + " and serializer " + serializer);
    }

    private static boolean nullOrInstance(final Object candidate, final Class<?> type) {
        if (candidate == null) {
            return true;
        }
        return type.isInstance(candidate);
    }
}
