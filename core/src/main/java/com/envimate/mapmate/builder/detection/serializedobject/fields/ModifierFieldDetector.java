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

package com.envimate.mapmate.builder.detection.serializedobject.fields;

import com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializationField;
import com.envimate.mapmate.shared.types.ClassType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.mapmate.mapper.serialization.serializers.serializedobject.SerializationField.fromPublicField;
import static com.envimate.mapmate.shared.types.resolver.ResolvedField.resolvedPublicFields;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModifierFieldDetector implements FieldDetector {

    public static FieldDetector modifierBased() {
        return new ModifierFieldDetector();
    }

    @Override
    public List<SerializationField> detect(final ClassType type) {
        return resolvedPublicFields(type).stream()
                .map(resolvedField -> fromPublicField(type, resolvedField))
                .collect(toList());
    }
}
