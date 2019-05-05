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

package com.envimate.mapmate.deserialization.builder.scanner;

import com.envimate.mapmate.deserialization.DeserializableDataTransferObject;
import com.envimate.mapmate.deserialization.DeserializableDefinitions;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethod;
import com.envimate.mapmate.deserialization.methods.DeserializationDTOMethodFactory;
import com.envimate.mapmate.filters.ClassFilter;
import com.envimate.mapmate.filters.ScanablePackage;
import com.envimate.mapmate.reflections.PackageName;

import java.util.List;
import java.util.Set;

import static com.envimate.mapmate.deserialization.DeserializableDataTransferObject.deserializableDataTransferObject;
import static com.envimate.mapmate.deserialization.DeserializableDefinitions.withTheDataTransferObjects;
import static com.envimate.mapmate.filters.ScanablePackage.scannablePackage;
import static java.util.stream.Collectors.toSet;

public final class DataTransferObjectPackageScanner implements PackageScanner {
    private final List<ClassFilter> classFilters;
    private final DeserializationDTOMethodFactory deserializationDTOMethodFactory;

    private DataTransferObjectPackageScanner(final List<ClassFilter> classFilters,
                                            final DeserializationDTOMethodFactory deserializationDTOMethodFactory) {
        this.classFilters = classFilters;
        this.deserializationDTOMethodFactory = deserializationDTOMethodFactory;
    }

    public static PackageScanner theDataTransferObjectPackageScanner(final List<ClassFilter> classFilters,
                                                                     final DeserializationDTOMethodFactory factory) {
        return new DataTransferObjectPackageScanner(classFilters, factory);
    }

    @Override
    public DeserializableDefinitions scan(final PackageName packageName) {
        final ScanablePackage scanablePackage = scannablePackage(packageName, this.classFilters);
        final List<Class<?>> types = scanablePackage.getTypes();
        final Set<DeserializableDataTransferObject<?>> dataTransferObjects = types.stream()
                .map(type -> {
                    try {
                        final DeserializationDTOMethod deserializer = this.deserializationDTOMethodFactory.createFor(type);
                        return deserializableDataTransferObject(
                                type,
                                deserializer
                        );
                    } catch (Exception e) {
                        throw new UnsupportedOperationException(String.format(
                                "Could not create DeserializationDTOMethod for type %s using factory %s",
                                type, this.deserializationDTOMethodFactory
                        ), e);
                    }
                })
                .collect(toSet());
        return withTheDataTransferObjects(dataTransferObjects);
    }
}
