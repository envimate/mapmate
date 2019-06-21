* [User Guide](#user-guide)
    * [Prerequisites](#prerequisites)
        * [Dependencies](#dependencies)
        * [Compiler Configuration](#compiler-configuration)
    * [Configuring MapMate Instance](#configuring-mapmate-instance)
        * [Registering Custom Primitives and Serialized Objects](#registering-custom-primitives-and-serialized-objects)
            * [PackageScanner](#packagescanner)
            * [Whitelisting and Blacklisting Packages and Classes](#whitelisting-and-blacklisting-packages-and-classes)
            * [Providing your Implementation](#providing-your-implementation)
            * [Skip Class Path Scanning](#skip-class-path-scanning)
    * [Default Conventions Explained](#default-conventions-explained)
        * [Overriding Default Conventions](#overriding-default-conventions)
            * [Detector](#detector)
            * [Custom Detection Factories](#custom-detection-factories)
            * [Annotations](#annotations)
    * [(Un)marshalling](#unmarshalling)
        * [JSON with Gson](#json-with-gson)
        * [JSON with ObjectMapper](#json-with-objectmapper)
        * [XML with X-Stream](#xml-with-x-stream)
        * [YAML with ObjectMapper](#yaml-with-objectmapper)
    * [Aggregating Validation Errors](#aggregating-validation-errors)
    * [FAQ](#faq)
  

# User Guide

This guide walks you through the features of MapMate, how to configure MapMate, and how to get most out of it.

Check out also our [Quick Start](QuickStart.md) if you only want to get started coding or take a look into the definition of [Custom Primitives](Concepts.md#custom-primitives) and [Serialized Objects](Concepts.md#serialized-objects) if you are wondering what those are.

## Prerequisites

### Dependencies

To use MapMate, you need to include the core jar as a dependency to your project. The latest version, with
examples of how to include it in the dependency management tool of your choice can be found in the [Maven Repository](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core) 

### Compiler Configuration

To deserialize a Serialized Object, MapMate needs to know the deserialization method's parameter names so that it can map the input field names to the method parameters. This means you need to compile your code with parameter names. That is achieved by passing the `-parameters` flag to the java compiler. More on the flag you can read in [javac official documentation](https://docs.oracle.com/en/java/javase/12/tools/javac.html) 

If your project is built with Maven, you must pass it to the compiler plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

Also, include this flag in your IDE's javac configuration, and make sure to rebuild your project afterwards to recompile with parameters.

* [Javac configuration in IntelliJ](https://www.jetbrains.com/help/idea/java-compiler.html#javac_eclipse)
* [Store information about method parameters in Eclipse](http://help.eclipse.org/2019-03/topic/org.eclipse.jdt.doc.user/reference/preferences/java/ref-preferences-compiler.htm)

## Configuring MapMate instance

MapMate comes with a builder, that allows you to configure how the Custom Primitives and Serialized Objects are detected, how they are (de)serialized, how they are (un)marshalled, how to handle exceptions. The resulting object gives you access to the [Serializer](../core/src/main/java/com/envimate/mapmate/serialization/Serializer.java) and [Deserializer](../core/src/main/java/com/envimate/mapmate/deserialization/Deserializer.java) that can be later used to respectively serialize and deserialize your objects. The resulting MapMate instance also gives you quick access to the most frequently used serialization and deserialization methods. 

This section addresses all the possible configurations and how they impact the (de)serialization of your objects. The MapMate instance is thread safe, so we encourage you to use a single instance in your application (unless you see a need of separately configured instances). 

For details on typical integration patterns check out the [Integration Guide](IntegrationGuide.md)

For a code example to start with and minimal configuration check out the [Quick Start](QuickStart.md) 

### Registering Custom Primitives and Serialized Objects

MapMate has a [default convention](#default-conventions-explained) of how to identify class being a Custom Primitive or a Serialized Object. In this section, we describe how to control which classes participate in the scanning. 

In it's simplest form, the builder provides the possibility to register a list of package names, that are scanned _recursively_ for Custom Primitives and Serialized Objects.

```java
final MapMateBuilder mapMateBuilder = MapMate.aMapMate(PACKAGE_TO_SCAN_1, PACKAGE_TO_SCAN_2, ...);
```

Checkout [ConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderTest.java) for some examples.

#### PackageScanner

The alternative builder of MapMate accepts a [PackageScanner](../core/src/main/java/com/envimate/mapmate/builder/PackageScanner.java). This interface has a single method `List<Class<?>> scan();` that is responsible for returning classes that are suspect to being a Custom Primitive or a Serialized Object.

#### Whitelisting and Blacklisting Packages and Classes

The default implementation of it - [DefaultPackageScanner](../core/src/main/java/com/envimate/mapmate/builder/DefaultPackageScanner.java) provides convenience method to whitelist or blacklist certain packages/classes.
 
Most of the time, your Custom Primitives and Serialized Objects end up in one or two packages. 
At the point, where you have a clear overview of your project structure, you might want to control which packages are scanned by MapMate to reduce startup times.

That is achieved by providing a configured instance of `DefaultPackageScanner` to the builder:

```java
        MapMate.aMapMate(DefaultPackageScanner.defaultPackageScanner(
            List.of(THE_PACKAGE_NAMES_TO_SCAN_RECURSIVELY),
            List.of(THE_LIST_OF_CLASSES_TO_INCLUDE),
            List.of(THE_PACKAGE_NAMES_TO_BLACKLIST_RECURSIVELY),
            List.of(THE_LIST_OF_CLASSES_TO_EXCLUDE))
        )
        .usingJsonMarshallers(gson::toJson, gson::fromJson)
        .build();
```

Checkout [ConventionalBuilderExclusionTest](../core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderExclusionTest.java) for some examples.

#### Providing your Implementation

As mentioned in [the section above](#packagescanner) the interface is quite simple, and if you want full control over which classes are scanned, you can also go ahead and implement the `PackageScanner` interface:

```java
        MapMate.aMapMate(new PackageScanner() {
            @Override
            public List<Class<?>> scan() {
                //do some custom logic 
                return classesToScan;
            }
        });
```

Only the classes returned by the PackageScanner participate later in the detection of Custom Primitives and Serialized Objects.    

#### Skip Class Path Scanning

With smaller projects, that have a conceivable number of Custom Primitives and/or Serialized Objects, or with those concerned with allowing any classpath scanning at all, MapMate builder allows individual registration of classes.

```java
MapMate.aMapMate()
    .withSerializedObjects(Email.class)
    .withCustomPrimitives(EmailAddress.class, Subject.class, Body.class)
    .usingJsonMarshallers(GSON::toJson, GSON::fromJson)
    .build();
```                                                                            

This configuration attempts to use the [Default Conventions](#default-conventions-explained) to register the `Email.class` as a Serialized Object and the `EmailAddress`, `Subject` and `Body` as Custom Primitives and does not scan any other class. 

If your method names differ from the defaults, you can also register the methods and fields that should be used for (de)serialization of the objects, using a functional interface for Custom Primitives and a name for Serialized Objects. 

```java
MapMate.aMapMate()
        .withSerializedObject(
              Email.class, 
              Email.class.getFields(), // the fields to use for serialization of Email.class
              "restore" // the name of the deserialization method to use for Email.class
        )
        .withCustomPrimitive(
                EmailAddress.class,
                EmailAddress::serialize, // Function<EmailAddress, String>
                EmailAddress::deserialize //Function<String, EmailAddress>
        )
        // ...    
```

We are aware that this is not the most comfortable way of achieving this goal, and we would like to know if the need
to configure the type manually is something desired by the community. Please let us know by opening an issue.

Checkout [IndividuallyAddedModelsBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/IndividuallyAddedModelsBuilderTest.java) for some examples.

## Default Conventions Explained

* MapMate respects the access modifiers and does not use any non-public field or method. Ever.
* MapMate scans the given package(s), visiting every class to identify whether it is a Custom Primitive or a Serialized Object. 
* A class is considered to be a Custom Primitive if it has a serialization method named "stringValue" and a static
 deserialization method named either "fromStringValue" or whose name contains the class name.
 
 Example:
 
 ```java
public final class EmailAddress {
    private final String value;

    public static EmailAddress anEmailAddress(final String value) {
        final String validated = EmailAddressValidator.ensureEmailAddress(value, "emailAddress");
        return new EmailAddress(validated);
    }

    public String stringValue() {
        return this.value;
    }
}
```
 
The method "anEmailAddress" is a valid deserialization method, since it's name contains the className. other valid names would be "deserialize", "theEmailAddressWithValue", etc.
 
* A class is considered to be a Serialized Object if it has a public static factory method name "deserialize". It is also
considered as such of the name of the class matches one of these patterns
```
.*DTO
.*Dto
.*Request
.*Response
.*State
```
and MapMate can find a "conventional deserialization method", which follows the algorithm:
1. If the class has a single factory method -> that's the one
2. alternatively, if there are multiple factory methods, the one called "deserialize" wins
3. if, for some reason, you have multiple factory methods named "deserialize", the one that has all the fields as parameters wins
4. alternatively, if there is no factory method called "deserialize", the factory methods named after the class are inspected with the same logic as point 3.

Example of the last point:

```java
public final class EmailDto {
    public final transient String saltInMySoup;
    public final EmailAddress sender;
    public final EmailAddress receiver;
    public final Subject subject;
    public final Body body;

    public static EmailDto emptyBodied(final EmailAddress sender,
                                       final EmailAddress receiver,
                                       final Subject subject) {
        return emailDto(sender, receiver, subject, Body.empty());
    }

    public static EmailDto emailDto(final EmailAddress sender,
                                    final EmailAddress receiver,
                                    final Subject subject,
                                    final Body body) {
        RequiredParameterValidator.ensureNotNull(sender, "sender");
        RequiredParameterValidator.ensureNotNull(receiver, "receiver");
        RequiredParameterValidator.ensureNotNull(body, "body");
        return new EmailDto("There it is", sender, receiver, subject, body);
    }
}
```

Here, the last method wins, since it is called `emailDto`. If we were to add another factory method called deserialize here, that one would be picked.

* Serialized Objects are serialized using the public fields(key:value) and deserialized using the same public factory method that 
was used to determine the class being a Serialized Object

Example of usage of the _Conventional_ MapMate can be found in [ConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderTest.java)


## Overriding Default Conventions

We understand that not everybody agrees with the way we decided to name the default methods. We made sure to provide you with builder methods to override any default conventions.

We've already discussed the [class and package scanning](#registering-custom-primitives-and-serialized-objects), in this part of the guide, we show how to override the Custom Primitive and Serialized Object detection mechanism itself.

### Detector

After the class has been filtered by the [PackageScanner](../core/src/main/java/com/envimate/mapmate/builder/PackageScanner.java) or by your [individual configuration](#skip-class-path-scanning) it is passed to the [Detector](../core/src/main/java/com/envimate/mapmate/builder/Detector.java), which identifies which of the classes are Custom Primitives/Serialized Objects. It does so by passing the classes through pre-defined list of [Custom Primitive Factories](../core/src/main/java/com/envimate/mapmate/builder/definitions/CustomPrimitiveDefinitionFactory.java) and [Serialized Object Factories](../core/src/main/java/com/envimate/mapmate/builder/definitions/SerializedObjectDefinitionFactory.java). 

If you only want to override the default method names, and/or the Serialized Object detection patterns you can use an instance of the [ConventionalDetector](../core/src/main/java/com/envimate/mapmate/builder/conventional/ConventionalDetector.java)
and configure the preferred Custom Primitive serialization/deserialization method names, 
Serialized Object deserialization method name, and class name patterns to use for Serialized Object.

```java
    public static MapMate mapMate() {
        return MapMate.aMapMate("com.envimate.examples")
                .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
                .withDetector(ConventionalDetector.conventionalDetector(
                        "myCustomPrimitiveSerializationMethodName",
                        "myCustomPrimitiveDeserializationMethodName",
                        "mySerializedObjectDeserializationMethodName",
                        ".*Dto"))
                .build();
    }
``` 

This example configuration scans the package `com.envimate.examples` recursively and considers a class being a Custom Primitive, if 

* it has a public method returning String that is called `myCustomPrimitiveSerializationMethodName`
* it has a public static factory method called `myCustomPrimitiveDeserializationMethodName` accepting a parameter of type `String` and returning an object of that class

and it will

* consider only classes, whose name ends with "Dto" to be Serialized Objects
* use their method called `mySerializedObjectDeserializationMethodName` for deserialization

Also take a look at [CustomConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/CustomConventionalBuilderTest.java) for some examples of customizing the conventions.

### Custom Detection Factories
If you have a specifically different Custom Primitive and/or a Serialized Object that does not fit any of the descriptions and
is not being located by MapMate, you can also provide your Factories for detecting them.

The conventional detector also accepts a list of factories for Custom Primitives and Serialized Objects.  All you need to do 
is provide an `analyze` method over the Class object, and construct a `CustomPrimitiveDefinition` or a `SerializedObjectDefinition` in case you deem the class to be either of those.

```java
MapMate.aMapMate("com.envimate.examples")
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

### Annotations

As mentioned before, we are in favour of _not_ polluting the domain code with framework specific code, that is then hard to get rid of. This includes annotations. However, we understand that there might be cases, that did not cross our mind, where your CustomPrimitives and SerializedObjects look unique, and MapMate needs an extra-kick to identify them. 
 We would like to know about those cases and try to come up with a proper abstraction that would allow you to configure those cases on the builder level. Still, if you are in a hurry and need to "just make it work for now", you can use the following annotations to indicate the custom primitive (de)serialization method, Serialized Object fields and
 deserialization method.
 
**Custom Primitives**

* [MapMatePrimitive](../core/src/main/java/com/envimate/mapmate/builder/conventional/customprimitives/classannotation/MapMatePrimitive.java) - class level, takes the (de)serialization method names as configuration
* [MapMatePrimitiveSerializer](../core/src/main/java/com/envimate/mapmate/builder/conventional/customprimitives/methodannotation/MapMatePrimitiveSerializer.java) - alternative to the class annotation, method level, marks the method as serialization for the Custom Primitive
* [MapMatePrimitiveDeserializer](../core/src/main/java/com/envimate/mapmate/builder/conventional/customprimitives/methodannotation/MapMatePrimitiveDeserializer.java) - alternative to the class annotation, method level, marks the method as deserialization for the Custom Primitive
    
**Serialized Objects**

* [MapMateSerializedField](../core/src/main/java/com/envimate/mapmate/builder/conventional/serializedobject/classannotation/MapMateSerializedField.java) - indicates that the field should be included in the serialization of the Serialized Object (the field should still be public since [MapMate does not access private fields](#default-conventions-explained))
* [MapMateDeserializationMethod](../core/src/main/java/com/envimate/mapmate/builder/conventional/serializedobject/classannotation/MapMateDeserializationMethod.java) - method level, marks it as a deserialization method for the Serialized Object.

For examples on Annotation-based mapmate instance, please check out the [AnnotationBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/AnnotationBuilderTest.java).

## (Un)marshalling

MapMate is unaware of the format you chose to represent the string value of your objects.
 
Upon receiving the String input, MapMate first asks the configured Unmarshaller to parse the String into a Map, and then, operates with that Map, and the definitions of the Custom Primitives and the Serialized Object to create the instance.

On the Serialization side of things, MapMate constructs a map of values using the Custom Primitive and Serialized Objects definitions, and then passes that map to the configured Marshaller so that it outputs the objects in a chosen format.

The (Un)marshallers can be registered in the builder, by specifying the type and the instance of the (Un)marshaller.

```java
public MapMateBuilder usingMarshallers(final Map<MarshallingType, Marshaller> marshallerMap,
                                       final Map<MarshallingType, Unmarshaller> unmarshallerMap) {}
```

There are of course convenience methods to register common Marshalling types, such as JSON, XML, YAML.

```java
public MapMateBuilder usingJsonMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {}
```

```java
public MapMateBuilder usingYamlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {}
```

```java
public MapMateBuilder usingXmlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {}
```

In this section, we show registration of some commonly used marshalling libraries for each of those types.

### JSON with GSON

Assuming you have a configured instance of `Gson` class, adding it as a JSON Marshaller for MapMate looks like:

```java
final Gson gson = new Gson(); // can be further configured depending on your needs.
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
                               .usingJsonMarshallers(gson::toJson, gson::fromJson)
                               .build();
```

### JSON with ObjectMapper

```java
MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
            .usingJsonMarshallers(objectMapper::writeValueAsString, objectMapper::readValue)
            .build();
```

Checkout [ObjectMapperConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/ObjectMapperConventionalBuilderTest.java) for an example.

### XML with X-Stream
```java
final XStream xStream = new XStream(new DomDriver());
xStream.alias("root", Map.class);

MapMate.aMapMate("com.envimate.mapmate.builder.models")
                .usingJsonMarshallers(xStream::toXML, new Unmarshaller() {
                    @Override
                    public <T> T unmarshal(final String input, final Class<T> type) {
                        return (T) xStream.fromXML(input, type);
                    }
                })
                .build();
```

Checkout [XmlBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/XmlBuilderTest.java) for an example.

note: If you wish to marshall in/from XML, don't forget to add the appropriate dependency:

```xml
<dependency>
    <groupId>xstream</groupId>
    <artifactId>xstream</artifactId>
    <version>latest</version>
</dependency>
```

### Yaml with ObjectMapper

```java
final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

return MapMate.aMapMate("com.envimate.mapmate.builder.models")
        .usingJsonMarshallers(objectMapper::writeValueAsString, objectMapper::readValue)
        .withExceptionIndicatingValidationError(CustomTypeValidationException.class)
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

MapMate _does not_ ship with these libraries, so you need to configure the marshaller of your choice also in the dependencies of your project. 

As you can see the format does not matter, and you can freely provide your  Marshalling mechanism, by implementing the [Marshaller](../core/src/main/java/com/envimate/mapmate/serialization/Marshaller.java) and [Unmarshaller](../core/src/main/java/com/envimate/mapmate/deserialization/Unmarshaller.java) interfaces.

We'll be happy to receive contributions to the documentation section here as well, with the typical marshalling libraries you use.


## Aggregating Validation Errors

For the rationale behind Validation Errors checkout the [Concepts page](Concepts.md#validation-errors)

By default, MapMate does not aggregate exceptions and simply returns an instance of [UnrecognizedExceptionOccurredException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/UnrecognizedExceptionOccurredException.java).

To enable reporting of aggregated messages, MapMate needs to be made aware of the validation exception (the exception class it needs to recognize as validation error). Assuming one has a single ValidationException somewhere in the domain that is thrown in the factory methods, in case the input is not valid, the MapMate configuration looks like:

```java
MapMate.aMapMate(YOUR_DOMAIN_PACKAGE)
        .usingJsonMarshallers(MARSHALLER, UNMARSHALLER)
        .withExceptionIndicatingValidationError(ValidationException.class)
        .build();
```

Given the Custom Primitive

**EmailAddress.class**
```java
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

And the Serialized Object

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

You can further customize the message of this error by giving in a lambda that maps your validation exception to an instance of a [ValidationError](../core/src/main/java/com/envimate/mapmate/deserialization/validation/ValidationError.java):

```java
MapMate.aMapMate(YOUR_PACKAGE)
    .usingJsonMarshallers(MARSHALLER, UNMARSHALLER)
    .withExceptionIndicatingValidationError(ValidationException.class,
            (exception, propertyPath) -> new ValidationError("This is a custom message we are reporting about "+ exception.getMessage(), propertyPath))
    .build();
```

will produce:

```bash
com.envimate.mapmate.deserialization.validation.AggregatedValidationException: deserialization encountered validation errors. Validation error at 'receiver', This is a custom message we are reporting about Invalid email address: 'not-a-valid-receiver-value'; Validation error at 'sender', This is a custom message we are reporting about Invalid email address: 'not-a-valid-sender-value';
```

Web(service) frameworks usually offer a way to register global exception handlers that map an exception into a response. This is the place where you register a mapper that generates a response using the instance of [AggregatedValidationException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/AggregatedValidationException.java).

## Recipes

Recipes are extension points to the conventional MapMate implementation. These are little MapMate plugins we wrote for some common usecases such as (de)serialization of numeric data types.
These are also a great opportunity to contribute to MapMate. The [Recipe interface](../core/src/main/java/com/envimate/mapmate/builder/recipes/Recipe.java) is quite simple:

```java
public interface Recipe {
    void cook(MapMateBuilder mapMateBuilder);
}
```

As you can see, you get an instance of MapMateBuilder and can modify it to fit your usecase. Then in the builder:

```java
MapMate.aMapMate()
    .usingRecipe(myCustomizationOfMapMate())
    .build();
```   

We'll be happy to receive your requests for new Recipes and contributions!

### JSON Numeric Data Types

As explained in the [Concepts](Concepts.md#string-representation) we are operating based on Strings. In case you have full control over the data format, and you agree with this approach, we encourage you to use String as your data representation. 
For the other cases, we have provided Recipes to [Serialize Numeric Data Types to Strings](Recipes.md#serialization) and [Deserialize Numeric Types into Strings without Parsing](Recipes.md#deserialization)

## Injector



## FAQ

**Q: Do I have to provide both serialization and deserialization methods for my Custom Primitives and Serialized Objects?**

_A: No, you can have classes that contain only serialization or only deserialization methods. Examples of these are Request objects being only **deserialized** and Response objects only **serialized**_

