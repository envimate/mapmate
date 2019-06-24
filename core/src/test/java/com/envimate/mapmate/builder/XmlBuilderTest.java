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

package com.envimate.mapmate.builder;

import com.envimate.mapmate.builder.models.conventional.Body;
import com.envimate.mapmate.builder.models.conventional.Email;
import com.envimate.mapmate.builder.models.conventional.EmailAddress;
import com.envimate.mapmate.builder.models.conventional.Subject;
import com.envimate.mapmate.deserialization.Unmarshaller;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class XmlBuilderTest {

    public static final String EMAIL_XML = "<root>\n" +
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
            "</root>";
    public static final Email EMAIL = Email.deserialize(
            EmailAddress.fromStringValue("sender@example.com"),
            EmailAddress.fromStringValue("receiver@example.com"),
            Subject.fromStringValue("Hello"),
            Body.fromStringValue("Hello World!!!")
    );

    public static MapMate theXmlMapMateInstance() {
        final XStream xStream = new XStream(new DomDriver());
        xStream.alias("root", Map.class);

        return MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .usingJsonMarshaller(xStream::toXML, new Unmarshaller() {
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        return (T) xStream.fromXML(input, type);
                    }
                })
                .build();
    }

    @Test
    public void testEmailSerialization() {
        final String result = theXmlMapMateInstance().serializeToJson(EMAIL);
        Assert.assertEquals(EMAIL_XML, result);
    }

    @Test
    public void testEmailDeserialization() {
        final Email result = theXmlMapMateInstance().deserializeJson(EMAIL_XML, Email.class);
        Assert.assertEquals(EMAIL, result);
    }
}
