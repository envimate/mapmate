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

package com.envimate.mapmate.filters.paths;


import com.envimate.mapmate.filters.ScanablePackage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static com.envimate.mapmate.filters.paths.FilesystemPath.filesystemPath;
import static com.envimate.mapmate.filters.paths.ResourcesPath.resourcesPath;

public interface ResourcesPathThatWorksForFilesystemsAndJars {

    static ResourcesPathThatWorksForFilesystemsAndJars getTransparently(final String resourcesPath) {
        final ClassLoader classLoader = ScanablePackage.class.getClassLoader();
        try {
            final URL url = classLoader.getResource(resourcesPath);
            if (url == null) {
                throw new RuntimeException("Resource does not exist: " + resourcesPath);
            }
            final URI uri = url.toURI();
            final String uriScheme = uri.getScheme();
            if ("file".equals(uriScheme)) {
                return filesystemPath(uri);
            } else if (uriScheme.contains("jar")) {
                return resourcesPath(uri, resourcesPath);
            } else {
                throw new RuntimeException("Unknown uri scheme: '" + uriScheme + "' for URI '" + uri + "'");
            }
        } catch (final URISyntaxException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isDirectory();

    List<String> basenamesOfChildren();
}
