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

package com.envimate.examples.example4;

import com.envimate.examples.example4.domain.auth.AccountId;
import com.envimate.examples.example4.domain.auth.Password;
import com.envimate.examples.example4.domain.auth.UserAuth;
import com.envimate.examples.example4.domain.auth.UserName;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.Unmarshaller;
import com.envimate.mapmate.serialization.Marshaller;
import com.envimate.mapmate.serialization.Serializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Map;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Example4 {

    /*
        Serializing using an XML dataformat
     */
    public static final void xmlSerialization() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Marshaller() {
                    @Override
                    public String marshal(final Object o) {
                        final XStream xStream = new XStream(new DomDriver());
                        xStream.alias("root", Map.class);
                        return xStream.toXML(o);
                    }
                })
                .thatScansThePackage("com.envimate.examples.example4.domain")
                .forCustomPrimitives()
                .filteredBy(
                        allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example4.domain")
                .forDataTransferObjects()
                .filteredBy(
                        allBut(
                                allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                .thatAre().serializedByItsPublicFields()
                .build();

        final UserAuth userAuth = UserAuth.userAuth(
                AccountId.fromString("098431"),
                UserName.fromString("john_doe90"),
                Password.fromString("mysecret123")
        );

        final String json = serializer.serialize(userAuth);
        System.out.println(json);

//        Output:
//        <root>
//          <entry>
//            <string>userName</string>
//            <string>john_doe90</string>
//          </entry>
//          <entry>
//            <string>accountId</string>
//            <string>098431</string>
//          </entry>
//          <entry>
//            <string>password</string>
//            <string>mysecret123</string>
//          </entry>
//        </root>
    }

    /*
        Deserializing using an XML dataformat
     */
    public static final void xmlDeserialization() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Unmarshaller() {
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        final XStream xStream = new XStream(new DomDriver());
                        xStream.alias("root", Map.class);
                        return (T) xStream.fromXML(input, type);
                    }
                })
                .thatScansThePackage("com.envimate.examples.example4.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example4.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();

        final String json = "" +
                "<root>\n" +
                "  <entry>\n" +
                "    <string>userName</string>\n" +
                "    <string>john_doe90</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>accountId</string>\n" +
                "    <string>098431</string>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>password</string>\n" +
                "    <string>mysecret123</string>\n" +
                "  </entry>\n" +
                "</root>";

        final UserAuth userAuth = deserializer.deserialize(json, UserAuth.class);
        System.out.println(userAuth);
    }

}
