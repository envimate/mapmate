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

package com.envimate.mapmate.builder.recipes.marshallers.urlencoded;

import com.envimate.mapmate.MapMateBuilder;
import com.envimate.mapmate.builder.recipes.Recipe;
import com.envimate.mapmate.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedUnmarshaller.urlEncodedUnmarshaller;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlEncodedMarshallerRecipe implements Recipe {

    public static MarshallingType urlEncoded() {
        return MarshallingType.marshallingType("urlencoded");
    }

    public static UrlEncodedMarshallerRecipe urlEncodedMarshaller() {
        return new UrlEncodedMarshallerRecipe();
    }

    @Override
    public void cook(final MapMateBuilder mapMateBuilder) {
        mapMateBuilder.usingMarshaller(urlEncoded(), UrlEncodedMarshaller.urlEncodedMarshaller(), urlEncodedUnmarshaller());
    }
}
