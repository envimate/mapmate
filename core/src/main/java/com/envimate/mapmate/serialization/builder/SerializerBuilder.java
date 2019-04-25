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

package com.envimate.mapmate.serialization.builder;

import com.envimate.mapmate.marshalling.MarshallerRegistry;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.marshalling.MarshallerRegistry.marshallerRegistry;
import static com.envimate.mapmate.marshalling.MarshallingType.*;
import static com.envimate.mapmate.reflections.PackageName.fromString;
import static com.envimate.mapmate.serialization.SerializableCustomPrimitive.serializableCustomPrimitive;
import static com.envimate.mapmate.serialization.SerializableDataTransferObject.serializableDataTransferObject;
import static com.envimate.mapmate.serialization.SerializableDefinitions.*;
import static com.envimate.mapmate.serialization.Serializer.theSerializer;
import static com.envimate.mapmate.serialization.builder.CustomPrimitiveSerializationMethodBuilder.aCustomPrimitiveSerializationMethodBuilder;
import static com.envimate.mapmate.serialization.builder.DataTransferObjectSerializationMethodBuilder.aDataTransferObjectSerializationMethodBuilder;
import static com.envimate.mapmate.serialization.builder.ScannablePackageBuilder.aScannablePackageBuilder;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static com.envimate.mapmate.validators.RequiredStringValidator.validateNotNullNorEmpty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializerBuilder {
    private final Map<MarshallingType, Marshaller> marshallers = new HashMap<>();
    private final List<SerializableDefinitions> definitions = new LinkedList<>();

    public static SerializerBuilder aSerializerBuilder() {
        return new SerializerBuilder();
    }

    public MarshallerStage marshallingTheType(final MarshallingType marshallingType) {
        validateNotNull(marshallingType, "marshallingType");
        return marshaller -> {
            validateNotNull(marshaller, "marshaller");
            this.marshallers.put(marshallingType, marshaller);
            return this;
        };
    }

    public SerializerBuilder withJsonMarshaller(final Marshaller marshaller) {
        return marshallingTheType(json()).using(marshaller);
    }

    public SerializerBuilder withXmlMarshaller(final Marshaller marshaller) {
        return marshallingTheType(xml()).using(marshaller);
    }

    public SerializerBuilder withYamlMarshaller(final Marshaller marshaller) {
        return marshallingTheType(yaml()).using(marshaller);
    }

    public ScannablePackageBuilder thatScansThePackage(final String packageName) {
        validateNotNullNorEmpty(packageName, "packageName");
        return aScannablePackageBuilder(packageScanner -> {
            final SerializableDefinitions definitions = packageScanner.scan(fromString(packageName));
            this.definitions.add(definitions);
            return this;
        });
    }

    public <T> CustomPrimitiveSerializationMethodBuilder<T> withCustomPrimitive(final Class<T> type) {
        validateNotNull(type, "type");
        return aCustomPrimitiveSerializationMethodBuilder(type, serializationCPMethod -> {
            final SerializableCustomPrimitive customPrimitive = serializableCustomPrimitive(type, serializationCPMethod);
            final SerializableDefinitions definitions = withASingleCustomPrimitive(customPrimitive);
            this.definitions.add(definitions);
            return this;
        });
    }

    public DataTransferObjectSerializationMethodBuilder withDataTransferObject(final Class<?> type) {
        validateNotNull(type, "type");
        return aDataTransferObjectSerializationMethodBuilder(serializationDTOMethod -> {
            final SerializableDataTransferObject dataTransferObject = serializableDataTransferObject(type,
                    serializationDTOMethod);
            final SerializableDefinitions definitions = withASingleDataTransferObject(dataTransferObject);
            this.definitions.add(definitions);
            return this;
        });
    }

    public Serializer build() {
        SerializableDefinitions allDefinitions = empty();
        for (final SerializableDefinitions definitions : this.definitions) {
            allDefinitions = merge(allDefinitions, definitions);
        }
        final MarshallerRegistry<Marshaller> marshallerRegistry = marshallerRegistry(this.marshallers);
        return theSerializer(marshallerRegistry, allDefinitions);
    }
}
