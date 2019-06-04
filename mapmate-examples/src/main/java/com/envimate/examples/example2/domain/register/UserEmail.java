package com.envimate.examples.example2.domain.register;

import com.envimate.examples.example2.exceptions.InvalidEmailException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UserEmail {

    private static final String EMAIL_RULES = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
    private final String value;

    private UserEmail(String value) {
        this.value = value;
    }

    public static final UserEmail fromString(String value) {
        final UserEmail userEmail = new UserEmail(value);
        if (userEmail.isValid()) {
            return userEmail;
        } else {
            throw InvalidEmailException.invalidEmailException(value);
        }
    }

    private boolean isValid() {
        Pattern p = Pattern.compile(EMAIL_RULES, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(this.value);
        return m.find();
    }

    public String getValue() {
        return this.value;
    }

}
