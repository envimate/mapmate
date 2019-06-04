package com.envimate.examples.example2;

import com.envimate.examples.example2.domain.register.RegisterUserRequest;
import com.envimate.examples.example2.exceptions.InsecurePasswordException;
import com.envimate.examples.example2.exceptions.InvalidEmailException;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.validation.AggregatedValidationException;
import com.envimate.mapmate.deserialization.validation.ValidationError;
import com.google.gson.Gson;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument;

public final class Example2 {

    /*
        Simple Validation requires us to tell mapmate what exceptions can be interpreted as a validation error.
        In below example we've mapped two exceptions for that purpose. Typically validation happens when executing the
        factory methods provided by the domain.
     */
    public static final void simpleValidatedDeserialization() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example2.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example2.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(InsecurePasswordException.class, ValidationError::fromExceptionMessageAndPropertyPath)
                .mappingExceptionUsing(InvalidEmailException.class, ValidationError::fromExceptionMessageAndPropertyPath)
                .build();

        final String json = "" +
                "{" +
                "    \"userName\": \"john_doe90\"," +
                "    \"userEmail\": \"john_doe@invalidDomain\"," +
                "    \"password\": \"insecurePassword\"" +
                "}";

        try {
            final RegisterUserRequest registerUserRequest = deserializer.deserialize(json, RegisterUserRequest.class);
        } catch (final AggregatedValidationException e) {
            e.getValidationErrors().forEach(error -> {
                System.out.println(String.format("Message: '%s', for field '%s'",
                        error.message,
                        error.propertyPath));
            });
        }
    }

}
