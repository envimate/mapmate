package com.envimate.examples.example3;

import com.envimate.examples.example3.domain.register.RegisterUserRequest;
import com.envimate.examples.example3.exceptions.CountryNotSupportedException;
import com.envimate.examples.example3.exceptions.MissingParameterException;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.validation.AggregatedValidationException;
import com.envimate.mapmate.deserialization.validation.ValidationError;
import com.google.gson.Gson;

import java.util.List;
import java.util.stream.Collectors;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.allBut;
import static com.envimate.mapmate.filters.ClassFilters.allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument;

public final class Example3 {

    /*
        Nested Validation is configured similarly as the previous example except that the Address field in below example
        cannot be missing any fields. Also, the Country field cannot be anything but 'United Kingdom'. Normally this would mean
        that if the Country is faulty, the address will be faulty too. Fortunately, mapmate considers this and prevents
        any and all cascaded exceptions.

        Below we set address2.country to 'France' which should fail validation, note that MissingParametersException is
        not thrown. For address1 we simply didn't provide a country.

        To recap, when validation errors occur, mapmate will prevent further validation upwards the domain.
     */
    public static final void simpleNestedValidatedDeserialization() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example3.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example3.domain")
                .forDataTransferObjects()
                    .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                    .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(CountryNotSupportedException.class, ValidationError::fromExceptionMessageAndPropertyPath)
                .mappingExceptionUsing(MissingParameterException.class, ValidationError::fromExceptionMessageAndPropertyPath)
                .onValidationErrors(validationErrors -> {
                    throw AggregatedValidationException.fromList(validationErrors);
                })
                .build();

        final String json = "" +
                "{" +
                "    \"userName\": \"john_doe90\"," +
                "    \"userEmail\": \"john_doe@validDomain.com\"," +
                "    \"password\": \"securePassword!\"," +
                "    \"address1\": {" +
                "       \"streetName\": \"Westminster\"," +
                "       \"houseNr\": \"1\"," +
                "       \"postalCode\": \"SW1A 1AA\"," +
                "       \"city\": \"London\"" +
                "    }," +
                "    \"address2\": {" +
                "       \"streetName\": \"Westminster\"," +
                "       \"houseNr\": \"1\"," +
                "       \"postalCode\": \"SW1A 1AA\"," +
                "       \"city\": \"London\"," +
                "       \"country\": \"France\"" +
                "    }" +
                "}";

        try {
            final RegisterUserRequest registerUserRequest = deserializer.deserialize(json, RegisterUserRequest.class);
            System.out.println(registerUserRequest);
        } catch (final AggregatedValidationException e) {
            e.getValidationErrors().forEach(error -> {
                System.out.println(String.format("Validation error for field '%s': %s.",
                        error.propertyPath,
                        error.message));
            });
        }
    }

    /*
        Complex validation configuration is demonstrated below.

        1. Mapping multiple relative classes or interfaces will trigger according to how closely it's related.
            e.g. If a mapping exists for RuntimeException AND Exception, the highest assignable mapping will be executed.
        2. Multiple lamba's are accepted as a mapping. Enable us to do various tasks. There's also an additional method accepting
            lists of ValidationErrors. This allows you to discect an Exception, and have mapmate interpret it as multiple
            validation errors. See the example below.
        3. onValidationErrors determines what happens when an validation error has occured. The default behaviour is as shown
            below.
     */
    public static final void complexNestedValidatedDeserialization() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example3.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example3.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .mappingExceptionUsing(CountryNotSupportedException.class, ValidationError::fromExceptionMessageAndPropertyPath)
                .mappingExceptionUsingList(MissingParameterException.class, (e, p) -> {
                    MissingParameterException m = (MissingParameterException) e;
                    return m.missingFields.stream()
                            .map(property -> ValidationError.fromStringMessageAndPropertyPath("Missing parameter", property))
                            .collect(Collectors.toList());
                })
                .mappingExceptionUsing(RuntimeException.class,
                        (e, p) -> {
                            final String message = String.format("Something went wrong at %s", p);
                            return ValidationError.fromStringMessageAndPropertyPath(message, p);
                        })
                .mappingExceptionUsing(Exception.class, (e, path) -> ValidationError.fromStringMessageAndPropertyPath("Something went wrong!", path))
                .onValidationErrors(validationErrors -> {
                    throw AggregatedValidationException.fromList(validationErrors);
                })
                .build();

        final String json = "" +
                "{" +
                "    \"userName\": \"john_doe90\"," +
                "    \"userEmail\": \"john_doe@validDomain.com\"," +
                "    \"password\": \"securePassword!\"," +
                "    \"address1\": {" +
                "       \"houseNr\": \"1\"," +
                "       \"postalCode\": \"SW1A 1AA\"," +
                "       \"city\": \"London\"" +
                "    }," +
                "    \"address2\": {" +
                "       \"streetName\": \"Westminster\"," +
                "       \"houseNr\": \"1\"," +
                "       \"postalCode\": \"SW1A 1AA\"," +
                "       \"city\": \"London\"," +
                "       \"country\": \"France\"" +
                "    }" +
                "}";

        try {
            final RegisterUserRequest registerUserRequest = deserializer.deserialize(json, RegisterUserRequest.class);
            System.out.println(registerUserRequest);
        } catch (final AggregatedValidationException e) {
            e.getValidationErrors().forEach(error -> {
                System.out.println(String.format("Validation error for field '%s': %s.",
                        error.propertyPath,
                        error.message));
            });
        }
    }

}
