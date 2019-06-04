package com.envimate.examples.example2.domain.register;

import com.envimate.examples.example2.exceptions.InsecurePasswordException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Password {

    private static final String PASSWORD_RULES = "[^a-z0-9 ]";
    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static final Password fromString(String value) {
        final Password password = new Password(value);
        if (password.isSecure()) {
            return password;
        } else {
            throw InsecurePasswordException.insecurePasswordException(value);
        }
    }

    private boolean isSecure() {
        Pattern p = Pattern.compile(PASSWORD_RULES, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(this.value);
        return m.find();
    }

    public String getValue() {
        return this.value;
    }
}
