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

package com.envimate.mapmate.filters.paths;

import java.io.File;
import java.net.URI;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

final class FilesystemPath implements ResourcesPathThatWorksForFilesystemsAndJars {
    private final File fileOrDirectoryObject;

    private FilesystemPath(final File fileOrDirectoryObject) {
        this.fileOrDirectoryObject = fileOrDirectoryObject;
    }

    static ResourcesPathThatWorksForFilesystemsAndJars filesystemPath(final URI uri) {
        final File fileOrDirectoryObject = new File(uri);
        return new FilesystemPath(fileOrDirectoryObject);
    }

    @Override
    public boolean isDirectory() {
        return this.fileOrDirectoryObject.isDirectory();
    }

    @Override
    public List<String> basenamesOfChildren() {
        if (!isDirectory()) {
            throw new RuntimeException("Not a directory");
        }
        final File[] children = this.fileOrDirectoryObject.listFiles();
        return stream(ofNullable(children).orElse(new File[0]))
                .map(File::getName)
                .collect(toList());
    }
}
