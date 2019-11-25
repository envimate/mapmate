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
import com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe;
import com.envimate.mapmate.examples.domain.*;
import com.envimate.mapmate.marshalling.Unmarshaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.envimate.mapmate.builder.recipes.marshallers.jackson.JacksonMarshaller.jacksonMarshallerJson;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class MarshallingExamples {
    private static final String YOUR_PACKAGE_TO_SCAN = "com.envimate.mapmate.examples.domain";

    @Test
    public void urlEncodedExample() {
        //Showcase start urlencoded
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingRecipe(UrlEncodedMarshallerRecipe.urlEncodedMarshaller())
                .build();
        //Showcase end urlencoded

        final ComplexPerson object = ComplexPerson.deserialize(
                asList(FirstName.fromStringValue("Aaron"), FirstName.fromStringValue("Adam")),
                singletonList(Address.deserialize(
                        StreetName.fromStringValue("Nulla Street"),
                        HouseNumber.fromStringValue("7a"),
                        ZipCode.fromStringValue("423423"),
                        CityName.fromStringValue("Mankato"),
                        Region.fromStringValue("Mississippi"),
                        Country.fromStringValue("USA")
                )));

        //Showcase start urlencodedusage
        final String urlEncoded = mapMate.serializeTo(object, UrlEncodedMarshallerRecipe.urlEncoded());
        //Showcase end urlencodedusage

        assert urlEncoded.equals("addresses[0][houseNumber]=7a&addresses[0][zipCode]=423423&addresses[0][country]=USA&addresses[0][streetName]=Nulla+Street&addresses[0][region]=Mississippi&addresses[0][city]=Mankato&firstNames[0]=Aaron&firstNames[1]=Adam");
    }

    @Test
    public void jsonWithGsonExample() {
        //Showcase start jsonWithGson
        final Gson gson = new Gson(); // can be further configured depending on your needs.
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        //Showcase end jsonWithGson

        final String json = mapMate.serializeToJson(ComplexPerson.deserialize(
                asList(FirstName.fromStringValue("Aaron"), FirstName.fromStringValue("Adam")),
                singletonList(Address.deserialize(
                        StreetName.fromStringValue("Nulla Street"),
                        HouseNumber.fromStringValue("7a"),
                        ZipCode.fromStringValue("423423"),
                        CityName.fromStringValue("Mankato"),
                        Region.fromStringValue("Mississippi"),
                        Country.fromStringValue("USA")
                ))));
        assert json.equals("{\"addresses\":[{\"houseNumber\":\"7a\",\"zipCode\":\"423423\",\"country\":\"USA\",\"streetName\":\"Nulla Street\",\"region\":\"Mississippi\",\"city\":\"Mankato\"}],\"firstNames\":[\"Aaron\",\"Adam\"]}");
    }

    @Test
    public void jsonWithObjectMapperExample() {
        //Showcase start jsonWithObjectMapper
        final ObjectMapper objectMapper = new ObjectMapper();
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingJsonMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
                .build();
        //Showcase end jsonWithObjectMapper

        final String json = mapMate.serializeToJson(ComplexPerson.deserialize(
                asList(FirstName.fromStringValue("Aaron"), FirstName.fromStringValue("Adam")),
                singletonList(Address.deserialize(
                        StreetName.fromStringValue("Nulla Street"),
                        HouseNumber.fromStringValue("7a"),
                        ZipCode.fromStringValue("423423"),
                        CityName.fromStringValue("Mankato"),
                        Region.fromStringValue("Mississippi"),
                        Country.fromStringValue("USA")
                ))));
        assert json.equals("{\"addresses\":[{\"houseNumber\":\"7a\",\"zipCode\":\"423423\",\"country\":\"USA\",\"streetName\":\"Nulla Street\",\"region\":\"Mississippi\",\"city\":\"Mankato\"}],\"firstNames\":[\"Aaron\",\"Adam\"]}");
    }

    @Test
    public void xmlWithXStream() {
        //Showcase start xmlWithXStream
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
        //Showcase end xmlWithXStream

        final String xml = mapMate.serializeToXml(ComplexPerson.deserialize(
                asList(FirstName.fromStringValue("Aaron"), FirstName.fromStringValue("Adam")),
                singletonList(Address.deserialize(
                        StreetName.fromStringValue("Nulla Street"),
                        HouseNumber.fromStringValue("7a"),
                        ZipCode.fromStringValue("423423"),
                        CityName.fromStringValue("Mankato"),
                        Region.fromStringValue("Mississippi"),
                        Country.fromStringValue("USA")
                ))));
        assert xml.equals("" +
                "<root>\n" +
                "  <entry>\n" +
                "    <string>addresses</string>\n" +
                "    <list>\n" +
                "      <root>\n" +
                "        <entry>\n" +
                "          <string>houseNumber</string>\n" +
                "          <string>7a</string>\n" +
                "        </entry>\n" +
                "        <entry>\n" +
                "          <string>zipCode</string>\n" +
                "          <string>423423</string>\n" +
                "        </entry>\n" +
                "        <entry>\n" +
                "          <string>country</string>\n" +
                "          <string>USA</string>\n" +
                "        </entry>\n" +
                "        <entry>\n" +
                "          <string>streetName</string>\n" +
                "          <string>Nulla Street</string>\n" +
                "        </entry>\n" +
                "        <entry>\n" +
                "          <string>region</string>\n" +
                "          <string>Mississippi</string>\n" +
                "        </entry>\n" +
                "        <entry>\n" +
                "          <string>city</string>\n" +
                "          <string>Mankato</string>\n" +
                "        </entry>\n" +
                "      </root>\n" +
                "    </list>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>firstNames</string>\n" +
                "    <list>\n" +
                "      <string>Aaron</string>\n" +
                "      <string>Adam</string>\n" +
                "    </list>\n" +
                "  </entry>\n" +
                "</root>");
    }

    @Test
    public void yamlWithObjectMapper() {
        //Showcase start yamlWithObjectMapper
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                .usingYamlMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
                .build();
        //Showcase end yamlWithObjectMapper

        final String yaml = mapMate.serializeToYaml(ComplexPerson.deserialize(
                asList(FirstName.fromStringValue("Aaron"), FirstName.fromStringValue("Adam")),
                singletonList(Address.deserialize(
                        StreetName.fromStringValue("Nulla Street"),
                        HouseNumber.fromStringValue("7a"),
                        ZipCode.fromStringValue("423423"),
                        CityName.fromStringValue("Mankato"),
                        Region.fromStringValue("Mississippi"),
                        Country.fromStringValue("USA")
                ))));
        assert yaml.equals("" +
                "---\n" +
                "addresses:\n" +
                "- houseNumber: \"7a\"\n" +
                "  zipCode: \"423423\"\n" +
                "  country: \"USA\"\n" +
                "  streetName: \"Nulla Street\"\n" +
                "  region: \"Mississippi\"\n" +
                "  city: \"Mankato\"\n" +
                "firstNames:\n" +
                "- \"Aaron\"\n" +
                "- \"Adam\"\n");
    }

    @Test
    public void jacksonWithRecipe() {
        //Showcase start jacksonWithRecipe
        final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                //...
                .usingRecipe(jacksonMarshallerJson(new ObjectMapper()))
                //...
                .build();
        //Showcase end jacksonWithRecipe

        final String json = mapMate.serializeToJson(ComplexPerson.deserialize(
                asList(FirstName.fromStringValue("Aaron"), FirstName.fromStringValue("Adam")),
                singletonList(Address.deserialize(
                        StreetName.fromStringValue("Nulla Street"),
                        HouseNumber.fromStringValue("7a"),
                        ZipCode.fromStringValue("423423"),
                        CityName.fromStringValue("Mankato"),
                        Region.fromStringValue("Mississippi"),
                        Country.fromStringValue("USA")
                ))));
        assert json.equals("{\"addresses\":[{\"houseNumber\":\"7a\",\"zipCode\":\"423423\",\"country\":\"USA\",\"streetName\":\"Nulla Street\",\"region\":\"Mississippi\",\"city\":\"Mankato\"}],\"firstNames\":[\"Aaron\",\"Adam\"]}");
    }
}
