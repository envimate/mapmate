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

package com.envimate.mapmate.specs.givenwhenthen;

import com.envimate.mapmate.MapMate;
import com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe;
import com.envimate.mapmate.domain.valid.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.mapmate.MapMate.aMapMate;
import static com.envimate.mapmate.specs.givenwhenthen.Unmarshallers.*;
import static com.envimate.mapmate.specs.givenwhenthen.Marshallers.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Given {
    private final MapMate mapMate;

    public static Given given(final MapMate mapMate) {
        return new Given(mapMate);
    }

    public static Given givenTheExampleMapMateWithAllMarshallers() {
        final MapMate mapMate = aMapMate("com.envimate.mapmate.domain.valid")
                .usingJsonMarshaller(jsonMarshaller(), jsonUnmarshaller())
                .usingXmlMarshaller(xmlMarshaller(), xmlUnmarshaller())
                .usingYamlMarshaller(yamlMarshaller(), yamlUnmarshaller())
                .usingRecipe(UrlEncodedMarshallerRecipe.urlEncodedMarshaller())
                //.usingRecipe(manuallyRegisteredTypes()
                //        .withCustomPrimitives(AString.class, ANumber.class)
                //        .withSerializedObjects(AComplexType.class, AComplexTypeWithArray.class, AComplexNestedType.class)
                //)
                .withExceptionIndicatingValidationError(AnException.class)
                .build();
        return given(mapMate);
    }

    public When when() {
        return new When(this.mapMate);
    }
}
