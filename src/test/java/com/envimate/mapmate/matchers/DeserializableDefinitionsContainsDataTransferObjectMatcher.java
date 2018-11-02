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

package com.envimate.mapmate.matchers;

import com.envimate.mapmate.Definition;
import com.envimate.mapmate.deserialization.DeserializableDefinitions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;

public final class DeserializableDefinitionsContainsDataTransferObjectMatcher extends BaseMatcher<DeserializableDefinitions> {

    private final Class<?> type;

    private DeserializableDefinitionsContainsDataTransferObjectMatcher(final Class<?> type) {
        this.type = type;
    }

    public static Matcher<DeserializableDefinitions> containsValidDeserializableDTOForType(final Class<?> type) {
        return new DeserializableDefinitionsContainsDataTransferObjectMatcher(type);
    }

    @Override
    public boolean matches(final Object object) {
        final DeserializableDefinitions definitions = (DeserializableDefinitions) object;
        final Optional<Definition> definition = definitions.getDefinitionForType(this.type);
        return definition.map(Definition::isDataTransferObject).orElse(false);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("definitions should contain dataTransferObject ").appendValue(this.type);
    }
}
