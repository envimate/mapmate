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

import com.envimate.mapmate.filters.paths.ResourcesPathThatWorksForFilesystemsAndJars;
import com.envimate.mapmate.reflections.PackageName;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.mapmate.filters.paths.ResourcesPathThatWorksForFilesystemsAndJars.getTransparently;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public final class ScanablePackage {
    private final List<Class<?>> types;

    private ScanablePackage(final List<Class<?>> types) {
        this.types = types;
    }

    public static ScanablePackage scannablePackage(final PackageName packageName,
                                                   final List<ClassFilter> filters) {
        final Collection<Class<?>> classes = getClasses(packageName.internalValueForMapping());
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

    private static List<Class<?>> getClasses(final String packageName) {
        try {
            final ClassLoader classLoader = currentThread().getContextClassLoader();
            validateNotNull(classLoader, "classLoader");
            final String resourcesPath = packageNameToResourcesPath(packageName);
            final ResourcesPathThatWorksForFilesystemsAndJars path = getTransparently(resourcesPath);
            if (path.isDirectory()) {
                return path.basenamesOfChildren().stream()
                        .map(name -> getClasses(packageName + "." + name))
                        .flatMap(Collection::stream)
                        .collect(toList());
            } else {
                return convertClassResourcePathToClass(packageName);
            }
        } catch (final ClassNotFoundException e) {
            final String message = String.format("Could not scan classpath for classes of package %s", packageName);
            throw new UnsupportedOperationException(message, e);
        }
    }

    public List<Class<?>> getTypes() {
        return Collections.unmodifiableList(this.types);
    }

    private static List<Class<?>> convertClassResourcePathToClass(final String classResourcePath) throws ClassNotFoundException {
        final String suffix = ".class";
        if (classResourcePath.endsWith(suffix)) {
            final int endIndex = classResourcePath.length() - suffix.length();
            return singletonList(forName(classResourcePath.substring(0, endIndex)));
        }
        return new LinkedList<>();
    }

    private static String packageNameToResourcesPath(final String packageName) {
        final boolean fix = packageName.endsWith(".class");
        final String resourcesPath = packageName.replace('.', '/');
        if (fix) {
            return resourcesPath.replace("/class", ".class");
        }
        return resourcesPath;
    }
}
