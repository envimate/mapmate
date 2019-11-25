# Exception Handling

## Aggregating Validation Errors

For the rationale behind Validation Errors check out the [Concepts page](Concepts.md#validation-errors).

By default, MapMate does not aggregate exceptions and simply returns an instance of 
[UnrecognizedExceptionOccurredException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/UnrecognizedExceptionOccurredException.java).

To enable reporting of aggregated messages, MapMate needs to be made aware of the validation exception (the exception 
class it needs to recognize as validation error). Assuming one has a single ValidationException somewhere in the domain
that is thrown in the factory methods, in case the input is not valid, the MapMate configuration looks like:

<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/ExceptionExamples.java aggregateException)-->
```java
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingJsonMarshaller(GSON::toJson, GSON::fromJson)
        .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
        .build();
```

Given the custom primitive

**EmailAddress.class**
```java
public final class EmailAddress {
    private final String value;

    public static EmailAddress fromStringValue(final String value) {
        if(isValidEmailAddress(value)) {
            return new EmailAddress(value);
        } else {
            throw new CustomTypeValidationException(String.format("Invalid email address %s", value));
        }
    }
    ...
}
```

and the serialized object

**Email.class**

```java
public final class Email {
    public final EmailAddress sender;
    public final EmailAddress receiver;
    //...
}
```

Upon receiving invalid email addresses for both receiver and sender

```json
{
  "sender": "not-a-valid-sender-value",
  "receiver": "not-a-valid-receiver-value"
}
```

MapMate will now return an instance of [AggregatedValidationException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/AggregatedValidationException.java):

```bash
com.envimate.mapmate.deserialization.validation.AggregatedValidationException: deserialization encountered validation errors. Validation error at 'receiver', Invalid email address: 'not-a-valid-receiver-value'; Validation error at 'sender', Invalid email address: 'not-a-valid-sender-value';
```

You can further customize the message of this error by giving in a lambda that maps your validation exception to an 
instance of a 
[ValidationError](../core/src/main/java/com/envimate/mapmate/deserialization/validation/ValidationError.java):

<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/ExceptionExamples.java mappedException)-->
```java
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingJsonMarshaller(GSON::toJson, GSON::fromJson)
        .withExceptionIndicatingValidationError(CustomTypeValidationException.class,
                (exception, propertyPath) -> new ValidationError("This is a custom message we are reporting about " + exception.getMessage(), propertyPath))
        .build();
```

will produce:

```bash
com.envimate.mapmate.deserialization.validation.AggregatedValidationException: deserialization encountered validation errors. Validation error at 'receiver', This is a custom message we are reporting about Invalid email address: 'not-a-valid-receiver-value'; Validation error at 'sender', This is a custom message we are reporting about Invalid email address: 'not-a-valid-sender-value';
```

Web(service) frameworks usually offer a way to register global exception handlers that map an exception into a response.
This is the place where you register a mapper that generates a response using the instance of
[AggregatedValidationException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/AggregatedValidationException.java).
