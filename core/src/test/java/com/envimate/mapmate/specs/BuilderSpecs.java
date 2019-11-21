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

import com.envimate.mapmate.domain.valid.*;
import org.junit.Test;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.marshalling.MarshallingType.json;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;
import static java.util.Collections.emptyList;

public final class BuilderSpecs {

    @Test
    public void givenValidDataTransferObject_whenBuildingWithDataTransferObject_thenReturnsCorrectDeserializer() {
        given(
                aMapMate()
                        .withManuallyAddedTypes(AComplexType.class, AString.class, ANumber.class)
                        .build()
        )
                .when().theDefinitionsAreQueried()
                .theDefinitionsContainExactlyTheSerializedObjects(AComplexType.class)
                .theDefinitionsContainExactlyTheCustomPrimitives(AString.class, ANumber.class);
    }

    @Test
    public void givenValidCustomPrimitive_whenBuildingWithCustomPrimitive_thenReturnsCorrectDeserializer() {
        given(
                aMapMate().withManuallyAddedType(AString.class).build()
        )
                .when().theDefinitionsAreQueried()
                .theDefinitionsContainExactlyTheCustomPrimitives(AString.class)
                .theDefinitionsContainExactlyTheSerializedObjects();
    }

    @Test
    public void classesWithWildcardGenericsAreIgnored() {
        given(aMapMate().withManuallyAddedType(AComplexTypeWithTypeWildcards.class).build())
                .when().mapMateSerializes(AComplexTypeWithTypeWildcards.deserialize(emptyList())).withMarshallingType(json())
                .anExceptionIsThrownWithAMessageContaining("no definition found for type 'com.envimate.mapmate.domain.valid.AComplexTypeWithTypeWildcards'");
    }

    @Test
    public void classesWithTypeVariablesAreIgnored() {
        given(aMapMate().withManuallyAddedType(AComplexParameterizedType.class).build())
                .when().mapMateSerializes(AComplexParameterizedType.deserialize(emptyList())).withMarshallingType(json())
                .anExceptionIsThrownWithAMessageContaining("no definition found for type 'com.envimate.mapmate.domain.valid.AComplexParameterizedType'");
    }

    @Test
    public void collectionsWithTypeVariablesAreIgnored() {
        given(aMapMate().withManuallyAddedType(AComplexTypeWithWildcardedCollection.class).build())
                .when().mapMateSerializes(AComplexTypeWithWildcardedCollection.deserialize(emptyList())).withMarshallingType(json())
                .anExceptionIsThrownWithAMessageContaining("no definition found for type 'com.envimate.mapmate.domain.valid.AComplexTypeWithWildcardedCollection'");
    }

    @Test
    public void allKnownCollectionsAreSupported() {
        given(aMapMate().withManuallyAddedType(AComplexTypeWithDifferentCollections.class).build())
                .when().theDefinitionsAreQueried()
                .theDefinitionsContainExactlyTheSerializedObjects(AComplexTypeWithDifferentCollections.class)
                .theDefinitionsContainExactlyTheCustomPrimitives(ANumber.class);
    }
}
