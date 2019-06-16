#### Marshalling with GSON

```java
MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
        .build();
```

#### Marshalling with ObjectMapper

```java
 final ObjectMapper objectMapper = new ObjectMapper();
 final MapMate = MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshallers(value -> {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new UnsupportedOperationException("Could not parse value " + value, e);
            }
        }, new Unmarshaller() {
            @Override
            public <T> T unmarshal(final String input, final Class<T> type) {
                try {
                    return objectMapper.readValue(input, type);
                } catch (final IOException e) {
                    throw new UnsupportedOperationException("Could not parse value " + input + " to type " + type, e);
                }
            }
        })
        .build();
```


### Overriding Defaults

#### Packages and Classes to Scan

By default MapMate scans the given package recursively. To exclude or include more packages/classes use the MapMate 
builder that accepts an instance of `PackageScanner`. 

The `DefaultPackageScanner` is there for convenience, accepting a list of whitelist packages and classes and a list of
 blacklist packages and classes.  For example [`com.envimate.mapmate.builder.ConventionalBuilderExclusionTest`](https://github.com/envimate/mapmate/blob/master/core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderExclusionTest.java) uses:

```java
        MapMate.aMapMate(DefaultPackageScanner.defaultPackageScanner(
                List.of("com.envimate.mapmate.builder.models"),
                List.of(),
                List.of("com.envimate.mapmate.builder.models.excluded"),
                List.of())
        )
                .usingJsonMarshallers(gson::toJson, gson::fromJson)
                .build();
```

Will exclude the "excluded" package from the models. 

If you want full control over which classes are scanned, you can also go ahead and implement the `PackageScanner` 
interface:

```java
        MapMate.aMapMate(new PackageScanner() {
            @Override
            public List<Class<?>> scan() {
                //do some custom logic 
                return classesToScan;
            }
        });
```

#### Marshalling

You can configure marshallers per format using the convenience methods of the builder `usingJsonMarshallers`, `usingXmlMarshallers`, `usingYamlMarshallers`, or provide the whole Map of the marshallers, if you need support of all
formats using 

```java
MapMateBuilder usingMarshallers(final Map<MarshallingType, Marshaller> marshallerMap,
                                final Map<MarshallingType, Unmarshaller> unmarshallerMap){}
```

Let us know if you need more MarshallingTypes or feel free to contribute to the project with your own!

##### Json

```java
MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
        .build();
```

##### Xml using XStream

```java
final XStream xStream = new XStream(new DomDriver());
xStream.alias("root", Map.class);

return MapMate.aMapMate("com.envimate.examples")
        .usingXmlMarshallers(xStream::toXML, new Unmarshaller() {
            @Override
            public <T> T unmarshal(final String input, final Class<T> type) {
                return (T) xStream.fromXML(input, type);
            }
        })
        .build();
```

##### Yaml using ObjectMapper

```java
 final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
 final MapMate = MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshallers(value -> {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new UnsupportedOperationException("Could not parse value " + value, e);
            }
        }, new Unmarshaller() {
            @Override
            public <T> T unmarshal(final String input, final Class<T> type) {
                try {
                    return objectMapper.readValue(input, type);
                } catch (final IOException e) {
                    throw new UnsupportedOperationException("Could not parse value " + input + " to type " + type, e);
                }
            }
        })
        .build();
```

note: don't forget to add the appropriate dependency to use the YAMLFactory with the ObjectMapper.

```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>latest</version>
</dependency>
```

#### Validation Exceptions

By default MapMate does not aggregate exceptions and simply returns an instance of UnrecognizedExceptionOccurredException.
When you have a custom exception you throw in you Custom Primitives and Serialized Objects, indicating Validation exception, you can
instruct MapMate to treat those as sources of Validation errors. These will then be aggregated and reported together.

**EmailAddress.class**
```
public final class EmailAddress {
    private final String value;

    public static EmailAddress fromStringValue(final String value) {
        if(isValidEmailAddress(value)) {
            return new EmailAddress(value);
        } else {
            throw new ValidationException(String.format("Invalid email address %s", value));
        }
    }
    ...
}
```

Adding `ValidationException` to MapMate:

```java
    public static MapMate mapMate() {
        return MapMate.aMapMate("com.envimate.examples")
                .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
                .withExceptionIndicatingValidationError(ValidationException.class)
                .build();
    }
```

Now, upon receiving let's say several wrong email addresses in the request object, the reporting will look like:

```java
com.envimate.mapmate.deserialization.validation.AggregatedValidationException: deserialization encountered validation errors. Validation error at 'receiver', Invalid email address: 'not-a-valid-receiver-value'; Validation error at 'sender', Invalid email address: 'not-a-valid-sender-value';
```

You can further customize the message of this error by giving in a lambda that modifies the ValidationError before 
returning:

```java
return MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
        .withExceptionIndicatingValidationError(ValidationException.class,
                (exception, propertyPath) -> new ValidationError("This is a custom message we are reporting about"+ exception.getMessage(), propertyPath))
        .build();
```

will produce:

```java

com.envimate.mapmate.deserialization.validation.AggregatedValidationException: deserialization encountered validation errors. Validation error at 'receiver', This is a custom message we are reporting aboutInvalid email address: 'not-a-valid-receiver-value'; Validation error at 'sender', This is a custom message we are reporting aboutInvalid email address: 'not-a-valid-sender-value';
```

#### Overriding Detector

MapMate uses a so-call `Detector` to identify which classes are Custom Primitives, Serialized Objectss and how to (de)serialize them.
If you only want to override the default method names used for detection you can use an instance of the `ConventionalDetector`
and configure the preferred Custom Primitive serialization/deserialization method names, 
Serialized Object deserialization method name, and additional class name patterns to use.

```java
    public static MapMate mapMate() {
        return MapMate.aMapMate("com.envimate.examples")
                .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
                .withDetector(ConventionalDetector.conventionalDetector(
                        "myCustomPrimitiveSerializationMethodName",
                        "myCustomPrimitiveDeserializationMethodName",
                        "mySerializedObjectDeserializationMethodName",
                        ".*"))
                .build();
    }
``` 

Also take a look at [`com.envimate.mapmate.builder.CustomConventionalBuilderTest`](https://github.com/envimate/mapmate/blob/master/core/src/test/java/com/envimate/mapmate/builder/CustomConventionalBuilderTest.java) for some running examples.

### Advanced Configuration

#### Custom Construction Factories
If you have a specifically different Custom Primitive and/or a Serialized Object that does not fit any of the descriptions and
is not being located by MapMate, you can also provide your own Factories for detecting them.

The conventional detector also accepts a list of factories for Custom Primitives and Serialized Objects.  All you need to do 
is provide an `analyze` method over the Class object, and construct a `CustomPrimitiveDefinition` or a `SerializedObjectDefinition` with either or both a deserialization and serialization methods.

```java
return MapMate.aMapMate("com.envimate.examples")
        .withDetector(ConventionalDetector.conventionalDetector(
                List.of(
                        new CustomPrimitiveDefinitionFactory() {
                            @Override
                            public Optional<CustomPrimitiveDefinition> analyze(final Class<?> type) {
                                return Optional.of(customPrimitiveDefinition(type, mySerializationMethod, myDeserializationMethod));
                            }
                        }),
                List.of(
                        new SerializedObjectDefinitionFactory() {
                            @Override
                            public Optional<SerializedObjectDefinition> analyze(final Class<?> type) {
                                return Optional.of(SerializedObjectDefinition.serializedObjectDefinition(type, mySerializedFields, myDeserializationMethod));
                            }
                        })))
        .build();
```

#### Adding Definitions per Class Manually

As mentioned above, you can also skip the class path scanning and configure the objects manually in the builder:
```java
return MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
        .withCustomPrimitive(customPrimitiveDefinition(MyCustomPrimitive.class, myCustomPrimitiveSerializationMethod, myCustomPrimitiveDeserializationMethod))
        .withSerializedObject(serializedObjectDefinition(MySerializedObject.class, mySerializedObjectFields, mySerializedObjectDeserializationMethod))
        .build();
```

We are aware, that this is not the most comfortable way of achieving this goal, and we would like to know, if the need
to configure the type manually is something desired by the community. Please let us know.

#### Annotations

As mentioned before, we are in favor of _not_ polluting the domain code with framework specific code, that is then hard 
to get rid of. This includes annotations. However, we understand, that there might be cases, that did not cross our
 mind, where your CustomPrimitives and SerializedObjects look unique and MapMate needs an extra-kick to identify them. 
 We would like to know about those cases, and try to come up with a proper abstraction that would allow you to configure
 those cases on the builder level. Still, if you are in a hurry and need to "just make it work for now", you can use 
 the following annotations to indicate the custom primitive (de)serialization method, Serialized Object fields and
 deserialization method.
 
 ```
// Custom Primitives
MapMatePrimitive - class level, takes the (de)serialization method names as configuration
MapMatePrimitiveSerializer - method level
MapMatePrimitiveDeserializer - method level
    
// Serialized Objects
MapMateSerializedField - indicates that the field should be included in the serialization of the Serialized Object
MapMateDeserializationMethod - method level
```
 
For examples on Annotation-based mapmate please check the [`com.envimate.mapmate.builder.AnnotationBuilderTest`](https://github.com/envimate/mapmate/blob/master/core/src/test/java/com/envimate/mapmate/builder/AnnotationBuilderTest.java).