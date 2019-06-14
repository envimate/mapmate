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

package com.envimate.mapmate.serialization.builder.scanner;

import com.envimate.mapmate.filters.ClassFilter;
import com.envimate.mapmate.filters.ScanablePackage;
import com.envimate.mapmate.reflections.PackageName;
import com.envimate.mapmate.serialization.SerializableCustomPrimitive;
import com.envimate.mapmate.serialization.SerializableDefinitions;
import com.envimate.mapmate.serialization.methods.SerializationCPMethod;

import java.util.List;

import static com.envimate.mapmate.filters.ScanablePackage.scannablePackage;
import static com.envimate.mapmate.serialization.SerializableCustomPrimitive.serializableCustomPrimitive;
import static com.envimate.mapmate.serialization.SerializableDefinitions.withTheCustomPrimitives;
import static java.util.stream.Collectors.toList;

public final class CustomPrimitivePackageScanner implements PackageScanner {

    private final List<ClassFilter> classFilters;
    private final SerializationCPMethod serializationCPMethod;

    private CustomPrimitivePackageScanner(final List<ClassFilter> classFilters,
                                          final SerializationCPMethod serializationCPMethod) {
        this.classFilters = classFilters;
        this.serializationCPMethod = serializationCPMethod;
    }

    public static PackageScanner theCustomPrimitivePackageScanner(final List<ClassFilter> classFilters,
                                                                  final SerializationCPMethod serializationCPMethod) {
        return new CustomPrimitivePackageScanner(classFilters, serializationCPMethod);
    }

    @Override
    public SerializableDefinitions scan(final PackageName packageName) {
        final ScanablePackage scanablePackage = scannablePackage(packageName, this.classFilters);
        final List<Class<?>> types = scanablePackage.getTypes();
        final List<SerializableCustomPrimitive> customPrimitives = types.stream()
                .map(type -> serializableCustomPrimitive(type, this.serializationCPMethod))
                .collect(toList());
        return withTheCustomPrimitives(customPrimitives);
    }
}
