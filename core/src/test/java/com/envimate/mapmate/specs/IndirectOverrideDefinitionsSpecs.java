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

package com.envimate.mapmate.specs;

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.domain.valid.AComplexNestedType;
import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import com.envimate.mapmate.mapper.definitions.SerializedObjectDefinition;
import com.envimate.mapmate.mapper.deserialization.DeserializationFields;
import com.envimate.mapmate.mapper.deserialization.deserializers.serializedobjects.SerializedObjectDeserializer;
import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializationField;
import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializationFields;
import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializedObjectSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.envimate.mapmate.mapper.definitions.CustomPrimitiveDefinition.customPrimitiveDefinition;
import static com.envimate.mapmate.mapper.marshalling.MarshallingType.json;
import static com.envimate.mapmate.shared.types.ClassType.fromClassWithoutGenerics;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;
import static com.envimate.mapmate.specs.givenwhenthen.Marshallers.jsonMarshaller;
import static com.envimate.mapmate.specs.givenwhenthen.Unmarshallers.jsonUnmarshaller;

public final class IndirectOverrideDefinitionsSpecs {

    @Test
    public void customDeserializationForCustomPrimitiveOverridesIndirectDefault() {
        given(
                MapMate.aMapMate()
                        .withManuallyAddedType(AComplexType.class)
                        .withManuallyAddedDefinition(customPrimitiveDefinition(
                                fromClassWithoutGenerics(ANumber.class),
                                object -> "23",
                                value -> ANumber.fromInt(23)
                        ))
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("42").from(json()).toTheType(ANumber.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(ANumber.fromInt(23));
    }

    @Test
    public void customDeserializationForSerializedObjectOverridesIndirectDefault() {
        given(
                MapMate.aMapMate()
                        .withManuallyAddedType(AComplexNestedType.class)

                        .withManuallyAddedDefinition(SerializedObjectDefinition.serializedObjectDefinition(
                                fromClassWithoutGenerics(AComplexType.class),
                                SerializedObjectSerializer.serializedObjectSerializer(SerializationFields.serializationFields(
                                        List.of(SerializationField.serializationField(fromClassWithoutGenerics(AString.class), "foo", object -> AString.fromStringValue("bar")))
                                )).get(),
                                new SerializedObjectDeserializer() {
                                    @Override
                                    public Object deserialize(final Map<String, Object> elements) throws Exception {
                                        return AComplexType.deserialize(AString.fromStringValue("custom1"), AString.fromStringValue("custom2"), ANumber.fromInt(100), ANumber.fromInt(200));
                                    }

                                    @Override
                                    public DeserializationFields fields() {
                                        return DeserializationFields.deserializationFields(Map.of("foo", fromClassWithoutGenerics(AString.class)));
                                    }
                                }
                        ))

                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("{\"foo\": \"qwer\"}").from(json()).toTheType(AComplexType.class)
                .noExceptionHasBeenThrown()
                .theDeserializedObjectIs(AComplexType.deserialize(AString.fromStringValue("custom1"), AString.fromStringValue("custom2"), ANumber.fromInt(100), ANumber.fromInt(200)));
    }
}
