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

package com.envimate.mapmate.domain.valid;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "CollectionDeclaredAsConcreteClass"})
public final class AComplexTypeWithMap {
    public final Map<AString, ANumber> map;
    public final HashMap<AString, ANumber> hashMap;
    public final HashMap<AString, AComplexType> complexMap;

    public AComplexTypeWithMap(
            final Map<AString, ANumber> map,
            final HashMap<AString, ANumber> hashMap,
            final HashMap<AString, AComplexType> complexMap) {
        this.map = map;
        this.hashMap = hashMap;
        this.complexMap = complexMap;
    }

    public static AComplexTypeWithMap aComplexTypeWithMap(final Map<AString, ANumber> map,
                                                          final HashMap<AString, ANumber> hashMap1,
                                                          final HashMap<AString, AComplexType> hashMap2) {
        return new AComplexTypeWithMap(map, hashMap1, hashMap2);
    }
}
