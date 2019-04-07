/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.mapmate.deserialization.builder.scanner;

import com.envimate.mapmate.deserialization.DeserializableCustomPrimitive;
import com.envimate.mapmate.deserialization.DeserializableDefinitions;
import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.filters.ClassFilter;
import com.envimate.mapmate.filters.ScanablePackage;
import com.envimate.mapmate.reflections.PackageName;

import java.util.List;
import java.util.Set;

import static com.envimate.mapmate.deserialization.DeserializableCustomPrimitive.deserializableCustomPrimitive;
import static com.envimate.mapmate.filters.ScanablePackage.scannablePackage;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.withTheCustomPrimitives;
import static java.util.stream.Collectors.toSet;

public final class CustomPrimitivePackageScanner implements PackageScanner {

    private final List<ClassFilter> classFilters;
    private final DeserializationCPMethod deserializationCPMethod;

    private CustomPrimitivePackageScanner(final List<ClassFilter> classFilters,
                                          final DeserializationCPMethod deserializationCPMethod) {
        this.classFilters = classFilters;
        this.deserializationCPMethod = deserializationCPMethod;
    }

    public static PackageScanner theCustomPrimitivePackageScanner(final List<ClassFilter> classFilters,
                                                                  final DeserializationCPMethod deserializationCPMethod) {
        return new CustomPrimitivePackageScanner(classFilters, deserializationCPMethod);
    }

    @Override
    public DeserializableDefinitions scan(final PackageName packageName) {
        final ScanablePackage scanablePackage = scannablePackage(packageName, this.classFilters);
        final List<Class<?>> types = scanablePackage.getTypes();
        final Set<DeserializableCustomPrimitive<?>> customPrimitives = types.stream()
                .map(type -> deserializableCustomPrimitive(type, this.deserializationCPMethod))
                .collect(toSet());
        return withTheCustomPrimitives(customPrimitives);
    }
}
