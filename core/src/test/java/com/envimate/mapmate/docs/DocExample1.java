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
import com.envimate.mapmate.MapMateBuilder;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.google.gson.Gson;
import org.junit.Test;

public class DocExample1 {
    private static final String THE_PACKAGE_NAME_TO_SCAN_RECURSIVELY = "dewrfew";

    @Test
    public void example1() {
        //Showcase start example1
        final MapMate mapMate = MapMate.aMapMate(THE_PACKAGE_NAME_TO_SCAN_RECURSIVELY)
                .usingJsonMarshaller(new Gson()::toJson, new Gson()::fromJson)
                .build();
        //Showcase end example1

        Object myObject = new Object();
        //Showcase start serializeToJson
        System.out.println(mapMate.serializeToJson(myObject));
        //Showcase end serializeToJson

        final String myObjectAsJson = "fewfe"; // TODO
        // TODO
        //Showcase start deserializeJson
        myObject = mapMate.deserializeJson(myObjectAsJson, Object.class);
        //Showcase end deserializeJson


        //Showcase start serializeToYaml
        System.out.println(mapMate.serializeToYaml(myObject));
        //Showcase end serializeToYaml

        final String myObjectAsYaml = "gregre"; // TODO
        //Showcase start deserializeYaml
        myObject = mapMate.deserializeYaml(myObjectAsYaml, Object.class);
        //Showcase end deserializeYaml

        //Showcase start serializeToXml
        System.out.println(mapMate.serializeToXml(myObject));
        //Showcase end serializeToXml

        final String myObjectAsXml = "grgt"; // TODO
        //Showcase start deserializeXml
        myObject = mapMate.deserializeXml(myObjectAsXml, Object.class);
        //Showcase end deserializeXml


        //Showcase start serializeTo
        System.out.println(mapMate.serializeTo(myObject, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT")));
        //Showcase end serializeTo

        final String myObjectAsSomething = "frefre";
        //Showcase start deserialize
        myObject = mapMate.deserialize(myObjectAsSomething, Object.class, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
        //Showcase end deserialize
    }

    @Test
    public void showBuilder() {
        //Showcase start withoutPackageScanning
        final MapMate mapMate = MapMate.aMapMate()
                /* further configuration */
                .build();
        //Showcase end withoutPackageScanning
    }
}
