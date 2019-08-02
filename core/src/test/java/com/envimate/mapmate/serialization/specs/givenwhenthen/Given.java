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

package com.envimate.mapmate.serialization.specs.givenwhenthen;

import com.envimate.mapmate.domain.valid.*;
import com.envimate.mapmate.serialization.Serializer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshaller.urlEncodedMarshaller;
import static com.envimate.mapmate.marshalling.MarshallingType.urlEncoded;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;
import static com.envimate.mapmate.serialization.specs.givenwhenthen.Marshallers.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Given {
    private final Serializer serializer;

    public static Given givenTheExampleMapMateSerializer() {
        final Serializer serializer = aSerializer()
                .withJsonMarshaller(jsonMarshaller())
                .withXmlMarshaller(xmlMarshaller())
                .withYamlMarshaller(yamlMarshaller())
                .marshallingTheType(urlEncoded()).using(urlEncodedMarshaller())
                .withDataTransferObject(AComplexType.class)
                .serializedByItsPublicFields()
                .withDataTransferObject(AComplexTypeWithArray.class)
                .serializedByItsPublicFields()
                .withDataTransferObject(AComplexNestedType.class)
                .serializedByItsPublicFields()
                .withCustomPrimitive(AString.class)
                .serializedUsingTheMethod(AString::internalValueForMapping)
                .withCustomPrimitive(ANumber.class)
                .serializedUsingTheMethod(ANumber::internalValueForMapping)
                .build();
        return new Given(serializer);
    }

    public When when(final Object object) {
        return new When(this.serializer, object);
    }
}
