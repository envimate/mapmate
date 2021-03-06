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

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.newDirectoryStream;

final class ResourcesPath implements ResourcesPathThatWorksForFilesystemsAndJars {
    private final Path path;

    private ResourcesPath(final Path path) {
        this.path = path;
    }

    static ResourcesPath resourcesPath(final URI uri, final String resourcesPath) {
        final FileSystem fileSystem = getFileSystemOfUri(uri);
        final Path path = fileSystem.getPath(resourcesPath);
        return new ResourcesPath(path);
    }

    private static FileSystem getFileSystemOfUri(final URI uri) {
        try {
            final Map<String, String> env = new HashMap<>();
            return FileSystems.newFileSystem(uri, env);
        } catch (final FileSystemAlreadyExistsException e) {
            return FileSystems.getFileSystem(uri);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String cleanBasename(final String dirtyBasename) {
        if (dirtyBasename.endsWith("/")) {
            return dirtyBasename.substring(0, dirtyBasename.length() - 1);
        }
        return dirtyBasename;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(this.path);
    }

    @Override
    public List<String> basenamesOfChildren() {
        if (!isDirectory()) {
            throw new RuntimeException("Not a directory");
        }
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(this.path)) {
            final List<String> basenames = new LinkedList<>();
            for (final Path subPath : directoryStream) {
                final Path filename = subPath.getFileName();
                if (filename == null) {
                    throw new RuntimeException(String.format(
                            "subPath entry of the directory stream of %s was null. This should never happen",
                            this.path
                    ));
                }
                final String basename = filename.toString();
                final String cleanBasename = cleanBasename(basename);
                basenames.add(cleanBasename);
            }
            return basenames;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
