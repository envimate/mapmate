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
import com.envimate.mapmate.reflections.PackageName;
import com.envimate.mapmate.filters.ScanablePackage;
import com.envimate.mapmate.serialization.SerializableDataTransferObject;
import com.envimate.mapmate.serialization.SerializableDefinitions;
import com.envimate.mapmate.serialization.methods.SerializationDTOMethod;

import java.util.List;
import java.util.Set;

import static com.envimate.mapmate.filters.ScanablePackage.scannablePackage;
import static com.envimate.mapmate.serialization.SerializableDataTransferObject.serializableDataTransferObject;
import static com.envimate.mapmate.serialization.SerializableDefinitions.withTheDataTransferObjects;
import static java.util.stream.Collectors.toSet;

public final class DataTransferObjectPackageScanner implements PackageScanner {
    private final List<ClassFilter> classFilters;
    private final SerializationDTOMethod serializationDTOMethod;

    private DataTransferObjectPackageScanner(final List<ClassFilter> classFilters,
                                            final SerializationDTOMethod serializationDTOMethod) {
        this.classFilters = classFilters;
        this.serializationDTOMethod = serializationDTOMethod;
    }

    public static PackageScanner theDataTransferObjectPackageScanner(final List<ClassFilter> classFilters,
                                                                     final SerializationDTOMethod serializationDTOMethod) {
        return new DataTransferObjectPackageScanner(classFilters, serializationDTOMethod);
    }

    @Override
    public SerializableDefinitions scan(final PackageName packageName) {
        final ScanablePackage scanablePackage = scannablePackage(packageName, this.classFilters);
        final List<Class<?>> types = scanablePackage.getTypes();
        final Set<SerializableDataTransferObject> dataTransferObjects = types.stream()
                .map(type -> serializableDataTransferObject(type, this.serializationDTOMethod))
                .collect(toSet());
        return withTheDataTransferObjects(dataTransferObjects);
    }
}
