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

import com.envimate.mapmate.domain.wildcards.AComplexTypeWithTypeWildcards;
import com.envimate.mapmate.domain.wildcards.AComplexTypeWithWildcardedCollection;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;

public final class TypeWildcardSpecs {

    @Test
    public void collectionsWithTypeWildcardsAreIgnored() {
        given(
                () -> aMapMate()
                        .withManuallyAddedType(AComplexTypeWithWildcardedCollection.class)
                        .build())
                .when().mapMateIsInstantiated()
                .anExceptionIsThrownWithAMessageContaining("Type 'java.util.List<?>' is not registered but needs to be in order to support deserialization of 'com.envimate.mapmate.domain.wildcards.AComplexTypeWithWildcardedCollection'.\n" +
                        "Log entries for 'java.util.List<?>'\n" +
                        "DefinitionsBuilder -> AComplexTypeWithWildcardedCollection -> SimpleDetector: rejecting 'java.util.List<?>' because: type is not supported because it contains wildcard generics (\"?\")");
    }

    @Test
    public void classesWithWildcardGenericsAreIgnored() {
        given(
                () -> aMapMate()
                        .withManuallyAddedType(AComplexTypeWithTypeWildcards.class)
                        .build()
        )
                .when().mapMateIsInstantiated()
                .anExceptionIsThrownWithAMessageContaining("Type 'java.util.List<?>' is not registered but needs to be in order to support deserialization of 'com.envimate.mapmate.domain.wildcards.AComplexTypeWithTypeWildcards'.\n" +
                        "Log entries for 'java.util.List<?>'\n" +
                        "DefinitionsBuilder -> AComplexTypeWithTypeWildcards -> SimpleDetector: rejecting 'java.util.List<?>' because: type is not supported because it contains wildcard generics (\"?\")");
    }
}
