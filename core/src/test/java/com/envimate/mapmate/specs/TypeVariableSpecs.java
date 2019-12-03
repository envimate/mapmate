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

import com.envimate.mapmate.domain.parameterized.AComplexParameterizedType;
import com.envimate.mapmate.domain.repositories.RepositoryWithTypeVariableReference;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.mapper.marshalling.MarshallingType.json;
import static com.envimate.mapmate.builder.recipes.scanner.ClassScannerRecipe.addAllReferencedClassesIs;
import static com.envimate.mapmate.shared.types.ClassType.fromClassWithoutGenerics;
import static com.envimate.mapmate.shared.types.unresolved.UnresolvedType.unresolvedType;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;
import static com.envimate.mapmate.specs.givenwhenthen.Marshallers.jsonMarshaller;
import static com.envimate.mapmate.specs.givenwhenthen.Unmarshallers.jsonUnmarshaller;

public final class TypeVariableSpecs {

    @Test
    public void aSerializedObjectWithTypeVariableFieldsCanBeSerialized() {
        given(
                aMapMate()
                        .withManuallyAddedType(unresolvedType(AComplexParameterizedType.class).resolve(fromClassWithoutGenerics(AString.class)))
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(AComplexParameterizedType.deserialize(AString.fromStringValue("foo")))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"value\": \"foo\"\n" +
                        "}");
    }

    @Test
    public void aSerializedObjectWithTypeVariableFieldsCanBeDeserialized() {
        given(
                aMapMate()
                        .withManuallyAddedType(unresolvedType(AComplexParameterizedType.class).resolve(fromClassWithoutGenerics(AString.class)))
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateDeserializes("" +
                "{\n" +
                "  \"value\": \"foo\"\n" +
                "}").from(json()).toTheType(unresolvedType(AComplexParameterizedType.class).resolve(fromClassWithoutGenerics(AString.class)))
                .noExceptionHasBeenThrown();
    }

    @Test
    public void aSerializedObjectWithTypeVariableFieldsCanBeRegisteredTwice() {
        given(
                aMapMate()
                        .withManuallyAddedType(unresolvedType(AComplexParameterizedType.class).resolve(fromClassWithoutGenerics(AString.class)))
                        .withManuallyAddedType(unresolvedType(AComplexParameterizedType.class).resolve(fromClassWithoutGenerics(ANumber.class)))
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(AComplexParameterizedType.deserialize(ANumber.fromInt(42)))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"value\": \"42\"\n" +
                        "}");
    }

    @Test
    public void aSerializedObjectWithTypeVariableCanBeFoundAsAReferenceOfAScannedClass() {
        given(
                aMapMate()
                        .usingRecipe(addAllReferencedClassesIs(RepositoryWithTypeVariableReference.class))
                        .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                        .build()
        )
                .when().mapMateSerializes(AComplexParameterizedType.deserialize(AString.fromStringValue("foo")))
                .withMarshallingType(json())
                .noExceptionHasBeenThrown()
                .theSerializationResultWas("" +
                        "{\n" +
                        "  \"value\": \"foo\"\n" +
                        "}");
    }
}
