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

package com.envimate.mapmate.serialization.serializers.serializedobject;

import com.envimate.mapmate.definitions.hub.FullType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializationFields {
    private final List<SerializationField> fields;

    public static SerializationFields serializationFields(final List<SerializationField> fields) {
        validateNotNull(fields, "fields");
        return new SerializationFields(fields);
    }

    public boolean isEmpty() {
        return this.fields.isEmpty();
    }

    public List<SerializationField> fields() {
        return unmodifiableList(this.fields);
    }

    public List<FullType> typesList() {
        return this.fields.stream()
                .map(SerializationField::type)
                .collect(toUnmodifiableList());

    }
}
