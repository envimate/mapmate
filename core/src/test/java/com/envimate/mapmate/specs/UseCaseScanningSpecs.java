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

import com.envimate.mapmate.domain.repositories.UseCase1;
import com.envimate.mapmate.domain.repositories.UseCase2;
import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import org.junit.jupiter.api.Test;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.builder.recipes.scanner.ClassScannerRecipe.addAllReferencedClassesIs;
import static com.envimate.mapmate.specs.givenwhenthen.Given.given;

public final class UseCaseScanningSpecs {

    @Test
    public void twoScannedUseCasesCanProvideTheSameType() {
        given(() ->
                aMapMate()
                        .usingRecipe(addAllReferencedClassesIs(UseCase1.class, UseCase2.class))
                        .build()
        )
                .when().theDefinitionsAreQueried()
                .noExceptionHasBeenThrown()
                .theDefinitionsContainExactlyTheSerializedObjects(AComplexType.class)
                .theDefinitionsContainExactlyTheCustomPrimitives(ANumber.class, AString.class);
    }
}
