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

package com.envimate.mapmate.definitions;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.util.Objects.isNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionMapper<X> {
    private X result = null;
    private final Definition definition;

    public static <X> DefinitionMapper<X> map(final Definition definition) {
        validateNotNull(definition, "definition");
        return new DefinitionMapper<>(definition);
    }

    public <T extends Definition> DefinitionMapper<X> forType(final Class<T> type, final Function<T, X> function) {
        if (type.isInstance(this.definition)) {
            this.result = function.apply((T) this.definition);
            validateNotNull(this.result, "result");
        }
        return this;
    }

    public DefinitionMapper<X> forCustomPrimitive(final Function<CustomPrimitiveDefinition, X> function) {
        return forType(CustomPrimitiveDefinition.class, function);
    }

    public DefinitionMapper<X> forSerializedObject(final Function<SerializedObjectDefinition, X> function) {
        return forType(SerializedObjectDefinition.class, function);
    }

    public X get() {
        if (isNull(this.result)) {
            throw new UnsupportedOperationException("Unsupported definition: " + this.definition);
        }
        return this.result;
    }
}
