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

package com.envimate.mapmate.docs;

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.marshalling.Unmarshaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.junit.Test;

import java.util.Map;

import static com.envimate.mapmate.builder.recipes.marshallers.jackson.JacksonMarshaller.jacksonMarshallerJson;

public final class MarshallingExamples {
    private static final String YOUR_PACKAGE_TO_SCAN = "gfre";

    @Test
    public void jsonWithGsonExample() {
        //Showcase start jsonWithGson
        final Gson gson = new Gson(); // can be further configured depending on your needs.
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        //Showcase end jsonWithGson
    }

    @Test
    public void jsonWithObjectMapperExample() {
        //Showcase start jsonWithObjectMapper
        final ObjectMapper objectMapper = new ObjectMapper();
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
                .build();
        //Showcase end jsonWithObjectMapper
    }

    @Test
    public void xmlWithXStream() {
        //Showcase start xmlWithXStream
        final XStream xStream = new XStream(new DomDriver());
        xStream.alias("root", Map.class);

        MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(xStream::toXML, new Unmarshaller() {
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        return (T) xStream.fromXML(input, type);
                    }
                })
                .build();
        //Showcase end xmlWithXStream
    }

    @Test
    public void yamlWithObjectMapper() {
        //Showcase start yamlWithObjectMapper
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingYamlMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
                .build();
        //Showcase end yamlWithObjectMapper
    }

    @Test
    public void jacksonWithRecipe() {
        //Showcase start jacksonWithRecipe
        final MapMate mapMate = MapMate.aMapMate()
                //...
                .usingRecipe(jacksonMarshallerJson(new ObjectMapper()))
                //...
                .build();
        //Showcase end jacksonWithRecipe
    }
}
