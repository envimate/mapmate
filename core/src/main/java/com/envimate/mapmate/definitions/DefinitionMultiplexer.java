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

import java.util.function.Consumer;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionMultiplexer {
    private boolean hasRun = false;
    private final Definition definition;

    // TODO put in Definition
    public static DefinitionMultiplexer multiplex(final Definition definition) {
        validateNotNull(definition, "definition");
        return new DefinitionMultiplexer(definition);
    }

    public <T extends Definition> DefinitionMultiplexer forType(final Class<T> type, final Consumer<T> action) {
        if(type.isInstance(this.definition)) {
            action.accept((T) this.definition);
            this.hasRun = true;
        }
        return this;
    }

    public DefinitionMultiplexer forCustomPrimitive(final Consumer<CustomPrimitiveDefinition> action) {
        return forType(CustomPrimitiveDefinition.class, action);
    }

    public DefinitionMultiplexer forSerializedObject(final Consumer<SerializedObjectDefinition> action) {
        return forType(SerializedObjectDefinition.class, action);
    }

    public DefinitionMultiplexer forCollection(final Consumer<CollectionDefinition> action) {
        return forType(CollectionDefinition.class, action);
    }

    public void throwExceptionForAllOtherDefinitionTypes() {
        if (!this.hasRun) {
            throw new UnsupportedOperationException("Unsupported definition: " + this.definition);
        }
    }
}
