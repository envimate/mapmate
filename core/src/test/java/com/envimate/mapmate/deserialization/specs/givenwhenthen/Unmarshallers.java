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

package com.envimate.mapmate.deserialization.specs.givenwhenthen;

import com.envimate.mapmate.deserialization.Unmarshaller;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

final class Unmarshallers {

    private Unmarshallers() {
    }

    static Unmarshaller jsonUnmarshaller() {
        final Gson gson = new Gson();
        return gson::fromJson;
    }

    static Unmarshaller xmlUnmarshaller() {
        final XmlMapper xmlMapper = new XmlMapper();
        return new Unmarshaller() {
            @Override
            public <T> T unmarshal(final String input, final Class<T> type) {
                try {
                    return xmlMapper.readValue(input, type);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    static Unmarshaller yamlUnmarshaller() {
        final DumperOptions options = new DumperOptions();
        final Yaml yaml = new Yaml(options);
        return new Unmarshaller() {
            @Override
            public <T> T unmarshal(final String input, final Class<T> type) {
                return yaml.load(input);
            }
        };
    }
}
