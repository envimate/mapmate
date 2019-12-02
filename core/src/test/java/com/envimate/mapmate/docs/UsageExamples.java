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
import com.envimate.mapmate.mapper.marshalling.MarshallingType;
import com.envimate.mapmate.mapper.marshalling.Unmarshaller;
import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class UsageExamples {
    private static final String YOUR_PACKAGE_TO_SCAN = Email.class.getPackageName();

    private static final Email EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );

    @Test
    public void usageWithJson() {
        //Showcase start jsonInstance
        final Gson gson = new Gson();
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        //Showcase end jsonInstance

        //Showcase start serializeToJson
        final String json = mapMate.serializeToJson(EMAIL);
        System.out.println(json);
        //Showcase end serializeToJson
        assert json.equals("{\"receiver\":\"receiver@example.com\",\"body\":\"Hello World!!!\",\"sender\":\"sender@example.com\",\"subject\":\"Hello\"}");

        //Showcase start deserializeJson
        final Email deserializedEmail = mapMate.deserializeJson(json, Email.class);
        //Showcase end deserializeJson
        assert deserializedEmail.equals(EMAIL);
    }

    @Test
    public void usageWithYaml() {
        //Showcase start yamlInstance
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingYamlMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
                .build();
        //Showcase end yamlInstance

        //Showcase start serializeToYaml
        final String yaml = mapMate.serializeToYaml(EMAIL);
        System.out.println(yaml);
        //Showcase end serializeToYaml
        assert yaml.equals("---\n" +
                "receiver: \"receiver@example.com\"\n" +
                "body: \"Hello World!!!\"\n" +
                "sender: \"sender@example.com\"\n" +
                "subject: \"Hello\"\n");

        //Showcase start deserializeYaml
        final Email deserializedEmail = mapMate.deserializeYaml(yaml, Email.class);
        //Showcase end deserializeYaml
        assert deserializedEmail.equals(EMAIL);
    }

    @Test
    public void usageWithXml() {
        //Showcase start xmlInstance
        final XStream xStream = new XStream(new DomDriver());
        xStream.alias("root", Map.class);
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingXmlMarshaller(xStream::toXML, new Unmarshaller() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        return (T) xStream.fromXML(input, type);
                    }
                })
                .build();
        //Showcase end xmlInstance

        //Showcase start serializeToXml
        final String xml = mapMate.serializeToXml(EMAIL);
        System.out.println(xml);
        //Showcase end serializeToXml
        assert xml.equals("" +
                "<root>\n" +
                "  <entry>\n" +
                "    <string>receiver</string>\n" +
                "    <string>receiver@example.com</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>body</string>\n" +
                "    <string>Hello World!!!</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>sender</string>\n" +
                "    <string>sender@example.com</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>subject</string>\n" +
                "    <string>Hello</string>\n" +
                "  </entry>\n" +
                "</root>");

        //Showcase start deserializeXml
        final Email deserializedEmail = mapMate.deserializeXml(xml, Email.class);
        //Showcase end deserializeXml
        assert deserializedEmail.equals(EMAIL);
    }

    @Test
    public void usageWithCustomFormat() {
        //Showcase start example1
        final Gson gson = new Gson();
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingMarshaller(MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"), gson::toJson, gson::fromJson)
                .build();
        //Showcase end example1

        //Showcase start serializeToCustomFormat
        final String customFormat = mapMate.serializeTo(EMAIL, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
        System.out.println(customFormat);
        //Showcase end serializeToCustomFormat
        assert customFormat.equals("{\"receiver\":\"receiver@example.com\",\"body\":\"Hello World!!!\",\"sender\":\"sender@example.com\",\"subject\":\"Hello\"}");

        //Showcase start deserializeCustomFormat
        final Email deserializedEmail = mapMate.deserialize(customFormat, Email.class, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
        //Showcase end deserializeCustomFormat
        assert deserializedEmail.equals(EMAIL);
    }

    @Test
    public void showBuilder() {
        //Showcase start withoutPackageScanning
        MapMate.aMapMate()
                /* further configuration */
                .build();
        //Showcase end withoutPackageScanning
    }
}
