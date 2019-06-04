package com.envimate.examples;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ExamplesTest {
    private static boolean isPresent() {
        try {
            final Method m = ExamplesTest.class.getMethod("isPresent0", Object.class);
            return isPresent0(null) & m.getParameters()[0].isNamePresent();
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPresent0(final Object param1) {
        return true;
    }

    @Test
    public void ensureThatMethodParameterNamesAreAvailableThroughReflection() {
        assertThat("javac command line option -parameters must be in use", isPresent(), is(true));
    }
}
