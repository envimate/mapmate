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

package com.envimate.mapmate.builder.detection.serializedobject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.*;

public interface FieldDetector {

    static FieldDetector modifierBased() {
        return field -> {
            final int modifiers = field.getModifiers();
            return isPublic(modifiers) && !isStatic(modifiers) && !isTransient(modifiers);
        };
    }

    static FieldDetector annotationBased(final Class<? extends Annotation> annotation) {
        validateNotNull(annotation, "annotation");
        return field -> field.isAnnotationPresent(annotation);
    }

    boolean useForSerialization(Field field);
}
