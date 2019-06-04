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

package com.envimate.mapmate.filters;

import com.envimate.mapmate.reflections.PackageName;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ScanablePackage {
    private final List<Class<?>> types;

    private ScanablePackage(final List<Class<?>> types) {
        this.types = types;
    }

    public static ScanablePackage scannablePackage(final PackageName packageName,
                                                   final List<ClassFilter> filters) {
        final Collection<Class<?>> classes = findClasses(packageName.internalValueForMapping());
        final List<Class<?>> filteredClasses = new LinkedList<>();
        classes.forEach(c -> {
            for (final ClassFilter filter : filters) {
                if (!filter.include(c)) {
                    return;
                }
            }
            filteredClasses.add(c);
        });
        return new ScanablePackage(filteredClasses);
    }

    private static List<Class<?>> findClasses(final String packageName) {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .whitelistPackages(packageName)
                .scan()) {
            return scanResult.getAllClasses().loadClasses();
        }
    }

    public List<Class<?>> getTypes() {
        return Collections.unmodifiableList(this.types);
    }
}
