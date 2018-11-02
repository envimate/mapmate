package com.envimate.examples.utils;

import java.util.UUID;

public final class Utils {

    public static final String randomToken() {
        return UUID.randomUUID().toString();
    }

    public static final String genericSerializer(final Object o) {
        return o.toString();
    }
}
