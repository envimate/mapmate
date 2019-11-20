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

import com.envimate.mapmate.definitions.hub.FullType;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationField;
import com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializationField.fromPublicField;
import static com.envimate.mapmate.serialization.serializers.serializedobject.SerializationFields.serializationFields;
import static com.envimate.mapmate.validators.NotNullValidator.validateNotNull;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotationFieldDetector implements FieldDetector {
    private final Class<? extends Annotation> annotation;

    public static FieldDetector annotationBased(final Class<? extends Annotation> annotation) {
        validateNotNull(annotation, "annotation");
        return new AnnotationFieldDetector(annotation);
    }

    @Override
    public SerializationFields detect(final FullType type) {
        final List<SerializationField> list = stream(type.type().getFields())
                .filter(field -> isPublic(field.getModifiers()))
                .filter(field -> !isStatic(field.getModifiers()))
                .filter(field -> !isTransient(field.getModifiers()))
                .filter(field -> field.isAnnotationPresent(this.annotation))
                .map(field -> fromPublicField(type, field))
                .collect(toList());
        return serializationFields(list);
    }
}
