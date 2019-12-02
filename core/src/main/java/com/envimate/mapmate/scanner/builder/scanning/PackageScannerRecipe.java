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

package com.envimate.mapmate.scanner.builder.scanning;

import com.envimate.mapmate.scanner.builder.DependencyRegistry;
import com.envimate.mapmate.scanner.builder.MapMateBuilder;
import com.envimate.mapmate.scanner.builder.contextlog.BuildContextLog;
import com.envimate.mapmate.scanner.builder.recipes.Recipe;
import com.envimate.mapmate.shared.types.ClassType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.mapmate.shared.types.ClassType.fromClassWithoutGenerics;
import static com.envimate.mapmate.shared.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PackageScannerRecipe implements Recipe {
    private final PackageScanner packageScanner;

    public static Recipe packageScannerRecipe(final PackageScanner packageScanner) {
        validateNotNull(packageScanner, "packageScanner");
        return new PackageScannerRecipe(packageScanner);
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder, final DependencyRegistry dependencyRegistry) {
            final BuildContextLog contextLog = mapMateBuilder.contextLog().stepInto(PackageScannerRecipe.class);
        this.packageScanner.scan().forEach(type -> mapMateBuilder.withManuallyAddedType(fromClassWithoutGenerics(type), contextLog));
    }
}
