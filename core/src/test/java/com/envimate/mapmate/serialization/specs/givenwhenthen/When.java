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

import com.envimate.mapmate.domain.valid.AComplexType;
import com.envimate.mapmate.domain.valid.ANumber;
import com.envimate.mapmate.domain.valid.AString;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.Serializer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class When {
    private final Serializer serializer;

    public Then theFullyInitializedExampleDtoIsSerializedTo(final MarshallingType marshallingType) {
        final AComplexType aComplexType = AComplexType.aComplexType(
                AString.fromString("asdf"),
                AString.fromString("qwer"),
                ANumber.fromInt(1),
                ANumber.fromInt(5));
        try {
            final String serialized = this.serializer.serialize(aComplexType, marshallingType);
            return new Then(serialized, null);
        } catch (final Exception e) {
            return new Then(null, e);
        }
    }
}
