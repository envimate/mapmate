package com.envimate.examples.example3.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public final class MissingParameterException extends RuntimeException {
    public final List<String> missingFields;

    private MissingParameterException(final String msg, final List<String> missingFields) {
        super(msg);
        this.missingFields = missingFields;
    }

    public static MissingParameterException missingParameterException(final List<String> missingFields) {
        return new MissingParameterException(
                String.format("missing values for the required fields %s", missingFields.stream().collect(Collectors.joining(", "))), missingFields);
    }
}
