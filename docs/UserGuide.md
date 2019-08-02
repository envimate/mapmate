# User Guide

This guide walks you through the features of MapMate, how to configure MapMate, and how to get most out of it.

Check out also our [Quick Start](QuickStart.md) if you only want to get started coding or take a look into the 
definition of [Custom Primitives](Concepts.md#custom-primitives) and 
[Serialized Objects](Concepts.md#serialized-objects) if you are wondering what those are.

## Contents
   * [Prerequisites](#prerequisites)
     * [Dependencies](#dependencies)
     * [Compiler Configuration](#compiler-configuration)
     * [Optionally use lombok](#optionally-use-lombok)
   * [Using MapMate](#using-mapmate)
     * [Serializing to JSON](#serializing-to-json)
     * [Deserializing from JSON](#deserializing-from-json)
     * [Serializing to YAML](#serializing-to-yaml)
     * [Deserializing from YAML](#deserializing-from-yaml)
     * [Serializing to XML](#serializing-to-xml)
     * [Deserializing from XML](#deserializing-from-xml)
     * [Serializing to YOUR_CUSTOM_FORMAT](#serializing-to-your_custom_format)
     * [Deserializing from YOUR_CUSTOM_FORMAT](#deserializing-from-your_custom_format)
     * [Beyond MapMate](#beyond-mapmate)
   * [Configuring the MapMate instance](#configuring-the-mapmate-instance)
     * [Package Scanning](#package-scanning)
       * [PackageScanner](#packagescanner)
       * [Whitelisting and Blacklisting Packages and Classes](#whitelisting-and-blacklisting-packages-and-classes)
       * [Providing your own PackageScanner](#providing-your-own-packagescanner)
       * [Disable package scanning](#disable-package-scanning)
     * [Default Conventions Explained](#default-conventions-explained)
       * [Custom Primitives](#default-conventions-for-custom-primitives)
       * [Serialized Objects](#default-conventions-for-serialized-objects)
     * [Overriding Default Conventions](#overriding-default-conventions)
     * [Using different names / name patters](#using-different-names-/-name-patters)
     * [Manually registering exceptional cases](#manually-registering-exceptional-cases)
     * [Annotations](#annotations)
       * [Custom Primitives](#annotations-for-custom-primitives)
       * [Serialized Objects](#annotations-for-serialized-objects)
     * [Using a different ordered list of Custom Primitive/Serialized Object factories](#using-a-different-ordered-list-of-custom-primitiveserialized-object-factories)
     * [JSON with GSON](#json-with-gson)
     * [JSON with ObjectMapper](#json-with-objectmapper)
     * [XML with X-Stream](#xml-with-x-stream)
     * [Yaml with ObjectMapper](#yaml-with-objectmapper)
     * [application/x-www-form-urlencoded](#application/x-www-form-urlencoded)
   * [Aggregating Validation Errors](#aggregating-validation-errors)
   * [Recipes](#recipes)
     * [Using Recipes](#using-recipes)
       * [Jackson configuration support](#jackson-configuration-support)
       * [Support for language primitives (double, int, float, String, ...)](#support-for-language-primitives-double-int-float-string-)
       * [Support for manually registered types](#support-for-manually-registered-types)
     * [Crafting your own recipes](#crafting-your-own-recipes)
     * [The Builder Process](#the-builder-process)
     * [Understanding the Recipe Interface](#understanding-the-recipe-interface)
   * [FAQ](#faq)

## Prerequisites
MapMate is designed to be a slave of your code base. Hence it is very customizable and non-invasive and does not come
with many prerequisites. The ones it comes with are explained in this chapter.

### Dependencies
To use MapMate, you need to include the core jar as a dependency to your project. The latest version, with
examples of how to include it in the dependency management tool of your choice, can be found in the 
[Maven Repository](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core).

### Compiler Configuration
To deserialize a Serialized Object, MapMate needs to know the deserialization method's parameter names so that it can 
map the input field names to the method parameters. This means you need to compile your code with parameter names. That
is achieved by passing the `-parameters` flag to the java compiler. More on the flag you can read in
[javac official documentation](https://docs.oracle.com/en/java/javase/12/tools/javac.html).

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

Also, include this flag in your IDE's javac configuration, and make sure to rebuild your project afterwards the 
configuration changes.

* [Javac configuration in IntelliJ](https://www.jetbrains.com/help/idea/java-compiler.html#javac_eclipse)
* [Store information about method parameters in Eclipse](http://help.eclipse.org/2019-03/topic/org.eclipse.jdt.doc.user/reference/preferences/java/ref-preferences-compiler.htm)

### Optionally use Lombok
[Project Lombok](https://projectlombok.org) is giving the lazy coder a little relieve when coding Custom Primitives and
Serialized Objects by generating private constructors, `equals` and `hashCode` and `toString`. Check out their website 
for detailed instructions on how to include and use it in your project.  

## Using MapMate
Welcome to the most simple chapter of this user guide. Using MapMate is straight forward given that a configured 
MapMate instance is at your disposal already. The MapMate instance gives you quick access to the most frequently used 
serialization and deserialization methods. It is thread safe, so we encourage you to use a single instance in your 
application (unless you see a need of separately configured instances). 
 
### Minimal Configuration

MapMate needs to know the package where your [Custom Primitives](Concepts.md#custom-primitives) and [Serialized Objects](Concepts.md#serialized-objects) reside. MapMate is also unaware 
of the chosen format, hence needs to be configured with [(Un)marshaller](Concepts.md#unmarshalling) to deal with the format conversion. 

If you are following the [default conventions](UserGuide.md#default-conventions-explained), and have chosen JSON as format, along with Gson as marshaller, here is the minimal configuration you need to get access to `serializer` and `deserializer` 

```java
final MapMate mapMate = MapMate.aMapMate(THE_PACKAGE_NAME_TO_SCAN_RECURSIVELY)
        .usingJsonMarshallers(new Gson()::toJson, new Gson()::fromJson)
        .build();
```  

Below you can find detailed information about using this builder and configuring MapMate. Once you got hold of a MapMate instance you can then perform:
 
### Serializing to JSON
```java
System.out.println(mapMate.serializeToJson(myObject));
``` 
### Deserializing from JSON
```java
MyObject myObject = mapMate.deserializeJson(myObjectAsJson, MyObject.class);
``` 
### Serializing to YAML
```java
System.out.println(mapMate.serializeToYaml(myObject));
``` 
### Deserializing from YAML
```java
MyObject myObject = mapMate.deserializeYaml(myObjectAsYaml, MyObject.class);
``` 
### Serializing to XML
```java
System.out.println(mapMate.serializeToXml(myObject));
``` 
### Deserializing from XML
```java
MyObject myObject = mapMate.deserializeXml(myObjectAsXml, MyObject.class);
``` 
### Serializing to YOUR_CUSTOM_FORMAT
```java
System.out.println(mapMate.serializeTo(myObject, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT")));
``` 
### Deserializing from YOUR_CUSTOM_FORMAT
```java
MyObject myObject = mapMate.deserialize(myObjectAsSomething, MyObject.class, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
``` 

### Beyond MapMate
The MapMate instance also gives you access to the 
[Serializer](../core/src/main/java/com/envimate/mapmate/serialization/Serializer.java) and the
[Deserializer](../core/src/main/java/com/envimate/mapmate/deserialization/Deserializer.java). These are the powerful,
uncharted and undocumented areas of MapMate. Only the bravest of the bravest enter that area, and we are still looking
for a hero that will be brave enough to fill the black hole in its centre with documentation. Mysterious and powerful 
features like [Injection Support](../core/src/main/java/com/envimate/mapmate/injector/InjectorLambda.java) or
`Function<Map<String, Object>, Map<String, Object>> serializedPropertyInjector` are rumoured to be found there, but 
beware, with great power comes great responsibility.

## Configuring the MapMate instance
MapMate comes with a builder that allows you to configure how the Custom Primitives and Serialized Objects are 
detected, how they are (de)serialized, how they are (un)marshalled, how to handle exceptions. This section addresses 
all the possible configurations and how they impact the (de)serialization of your objects. 

For a code example to start with and minimal configuration check out the [Quick Start](QuickStart.md) 

### Package Scanning
MapMate ships with a package scanner that scans the list of packages it's been configured to scan for 
Custom Primitives and Serialized Objects. MapMate has a [default convention](#default-conventions-explained) of how to 
identify these special classes. In this section, we describe how to control which classes participate in the detection.

The builder provides the possibility to register a list of package names, that are scanned _recursively_:

```java
final MapMateBuilder mapMateBuilder = MapMate.aMapMate(PACKAGE_TO_SCAN_1, PACKAGE_TO_SCAN_2, ...)
    ...;
```

#### PackageScanner
The builder of MapMate accepts a 
[PackageScanner](../core/src/main/java/com/envimate/mapmate/builder/PackageScanner.java) as an alternative to the list 
of packages. This interface has a single method `List<Class<?>> scan();` that is responsible for returning classes that
are suspect to being a Custom Primitive or a Serialized Object:
```java
public interface PackageScanner {
    List<Class<?>> scan();
}
``` 

#### Whitelisting and Blacklisting Packages and Classes
You might want to control which packages are scanned by MapMate to reduce startup times or for other reasons. The 
[DefaultPackageScanner](../core/src/main/java/com/envimate/mapmate/builder/DefaultPackageScanner.java) provides 
factory methods that allow to whitelist or blacklist certain packages and/or classes: 

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

Checkout [ConventionalBuilderExclusionTest](../core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderExclusionTest.java) 
for some examples.

#### Providing your own PackageScanner
As mentioned in [the section above](#packagescanner), MapMate is expecting an instance of the PackageScanner interface.
That allows MapMate to use your own PackageScanning logic if you provide it with a proper implementation.    

#### Disable package scanning
There are cases, where classpath scanning is not a desired feature, e.g. high-security environments or serverless
platforms like AWS Lambda, where cold start costs are an issue. In these cases, just build MapMate without a list
of packages:

```java
final MapMateBuilder mapMateBuilder = MapMate.aMapMate()
    ...;
```  

With package scanning disabled, you need to either register your types manually or provide a list of classes, from which 
MapMate will detect your Custom Primitives and Serialized Objects. 
See 
[Support for manually registered types](#support-for-manually-registered-types)
for instructions on how to do that.

Also, checkout 
[IndividuallyAddedModelsBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/IndividuallyAddedModelsBuilderTest.java) 
for some simple examples.

### Default Conventions Explained
MapMate respects the access modifiers and does not use any non-public field or method. Ever. MapMate scans the given 
package(s), visiting every class to identify whether it is a Custom Primitive or a Serialized Object.

#### Default Conventions for Custom Primitives
A class is considered to be a Custom Primitive if it has a serialization method named "stringValue" and a static
deserialization method either named "fromStringValue" or whose name contains the class name.
A serialization method is public, returns an instance of String and takes no arguments. A deserialization method is 
public, static, returns an instance of the class it is declared in and takes one parameter of type String.
 
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
The method "anEmailAddress" is a valid deserialization method, since it's name contains the className. other valid 
names would be "deserialize", "theEmailAddressWithValue", etc.

#### Default Conventions for Serialized Objects 
A class is considered to be a Serialized Object if it has a public static factory method name "deserialize". 
Alternatively, is also considered as such if the name of the class matches one of the patterns
```
.*DTO
.*Dto
.*Request
.*Response
.*State
```
AND MapMate can find a "conventional deserialization method", which follows the algorithm:

1. If the class has a single factory method -> that's the one
2. alternatively, if there are multiple factory methods, the one called "deserialize" wins
3. if, for some reason, you have multiple factory methods named "deserialize", the one that has all the fields as 
parameters wins
4. alternatively, if there is no factory method called "deserialize", the factory methods named after the class are
inspected with the same logic as point 3.

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
Here, the last method wins, since it is called `emailDto`. If we were to add another factory method called deserialize 
here, that one would be picked.

Serialized Objects are serialized using the public fields(key:value) and deserialized using the same public factory
method that was used to determine the class being a Serialized Object

Example of usage of the _Conventional_ MapMate can be found in [ConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderTest.java)

### Overriding Default Conventions
We understand that not everybody agrees with the way we decided to name the default methods. We made sure to provide 
you with builder methods to override any default conventions.

### Using Different Names / Name Patterns
If you only want to override the default method names, and/or the Serialized Object detection patterns, you can use an 
instance of the 
[ConventionalDetector](../core/src/main/java/com/envimate/mapmate/builder/conventional/ConventionalDetector.java)
and configure the preferred Custom Primitive serialization/deserialization method names, Serialized Object 
deserialization method name, and class name patterns to use for Serialized Object.

```java
    public static MapMate mapMate() {
        return MapMate.aMapMate("com.envimate.examples")
                .usingJsonMarshaller(new Gson()::toJson, new Gson()::fromJson)
                .withDetector(ConventionalDetector.conventionalDetector(
                        "myCustomPrimitiveSerializationMethodName",
                        "myCustomPrimitiveDeserializationMethodName",
                        "mySerializedObjectDeserializationMethodName",
                        ".*Dto"))
                .build();
    }
```

Please note that all the string values accepted by the `conventionalDetector` factory method support regular 
expressions.

Also take a look at [CustomConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/CustomConventionalBuilderTest.java) 
for some examples of customizing the conventions.

### Manually registering exceptional cases
If there are only a few classes that are hard to detect using a convention, a valid technique is to manually teach
MapMate how to deal with them. Check out [Support for manually registered types](#Support-for-manually-registered-types)
for instructions on how to achieve that.

### Annotations
As mentioned before, we are in favour of _not_ polluting the domain with framework specific code, that is then hard to 
get rid of. This includes annotations. However, we understand that there might be cases, that did not cross our mind, 
where your CustomPrimitives and SerializedObjects look unique, and MapMate needs an extra-kick to identify them. 
We would like to know about those cases and try to come up with a proper abstraction that would allow you to configure
those cases on the builder level. Still, if you are in a hurry and need to "just make it work for now", you can use the
following annotations to indicate the custom primitive (de)serialization method, Serialized Object fields and 
deserialization method.
 
#### Annotations for Custom Primitives

* [MapMatePrimitive](../core/src/main/java/com/envimate/mapmate/builder/conventional/customprimitives/classannotation/MapMatePrimitive.java)
 class level, takes the (de)serialization method names as configuration
* [MapMatePrimitiveSerializer](../core/src/main/java/com/envimate/mapmate/builder/conventional/customprimitives/methodannotation/MapMatePrimitiveSerializer.java)
alternative to the class annotation, method level, marks the method as serialization for the Custom Primitive
* [MapMatePrimitiveDeserializer](../core/src/main/java/com/envimate/mapmate/builder/conventional/customprimitives/methodannotation/MapMatePrimitiveDeserializer.java)
alternative to the class annotation, method level, marks the method as deserialization for the Custom Primitive
    
#### Annotations for Serialized Objects

* [MapMateSerializedField](../core/src/main/java/com/envimate/mapmate/builder/conventional/serializedobject/classannotation/MapMateSerializedField.java)
indicates that the field should be included in the serialization of the Serialized Object (the field should still be 
public since [MapMate does not access private fields](#default-conventions-explained))
* [MapMateDeserializationMethod](../core/src/main/java/com/envimate/mapmate/builder/conventional/serializedobject/classannotation/MapMateDeserializationMethod.java)
method level, marks it as a deserialization method for the Serialized Object.

For examples on Annotation-based mapmate instance, please check out the
[AnnotationBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/AnnotationBuilderTest.java).

### Using a Different Ordered List of Custom Primitive/Serialized Object Factories
If the above is not enough, don't worry, you've got yourself covered by telling MapMate to use a custom instance of
[ConventionalDetector](../core/src/main/java/com/envimate/mapmate/builder/conventional/ConventionalDetector.java) that
is using your own implementations of 
[CustomPrimitiveDefinitionFactory](../core/src/main/java/com/envimate/mapmate/builder/definitions/CustomPrimitiveDefinitionFactory.java) 
and 
[SerializedObjectDefinitionFactory](../core/src/main/java/com/envimate/mapmate/builder/definitions/SerializedObjectDefinitionFactory.java)
to determine which classes are Custom Primitives/Serialized Objects and how to use them. 

Check out the existing implementations used in the other factory methods of the `ConventionalDetector` as well as
[The Builder Process](#the-builder-process) for inspiration.


## (Un)marshalling
MapMate is unaware of the format you chose to represent the string value of your objects. Upon receiving the String 
input, MapMate first asks the configured Unmarshaller to parse the String into a Map, and then, operates with that Map,
and the definitions of the Custom Primitives and the Serialized Object to create the instance.

On the Serialization side of things, MapMate constructs a map of values using the Custom Primitive and 
Serialized Objects definitions, and then passes that map to the configured Marshaller so that it outputs the objects in
a chosen format.

There are convenience methods to register common Marshalling types, such as JSON, XML, YAML.

```java
public MapMateBuilder usingJsonMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {}
```

```java
public MapMateBuilder usingYamlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {}
```

```java
public MapMateBuilder usingXmlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller) {}
```

The (Un)marshallers can also be registered in the builder, by providing 2 maps with the marshalling type as key and an 
instance of the corresponding (Un)marshaller as value.

```java
public MapMateBuilder usingMarshallers(final Map<MarshallingType, Marshaller> marshallerMap,
                                       final Map<MarshallingType, Unmarshaller> unmarshallerMap) {}
```

In this section, we show the registration of some commonly used marshalling libraries for each of those types. Also,
check out [Jackson configuration support](#jackson-configuration-support) in the recipe collection section.

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
    <version>${xstream.version}</version>
</dependency>
```

### Yaml with ObjectMapper

```java
final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

return MapMate.aMapMate("com.envimate.mapmate.builder.models")
        .usingYamlMarshallers(objectMapper::writeValueAsString, objectMapper::readValue)
        .build();
```

note: don't forget to add the appropriate dependency to use the YAMLFactory with the ObjectMapper.

```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>${jackson.version}</version>
</dependency>
```

MapMate _does not_ ship with these libraries, so you need to configure the marshaller of your choice also in the 
dependencies of your project. 

### application/x-www-form-urlencoded
```json
return MapMate.aMapMate("com.envimate.mapmate.builder.models")
        .usingRecipe(urlEncodedMarshaller())
        .build();
```

This does not require an external library.

As you can see the format does not matter, and you can freely provide your Marshalling mechanism, by implementing the
[Marshaller](../core/src/main/java/com/envimate/mapmate/serialization/Marshaller.java) and 
[Unmarshaller](../core/src/main/java/com/envimate/mapmate/deserialization/Unmarshaller.java) interfaces.

We'll be happy to receive contributions to the documentation section here as well, with the typical marshalling 
libraries you use.

## Aggregating Validation Errors

For the rationale behind Validation Errors check out the [Concepts page](Concepts.md#validation-errors).

By default, MapMate does not aggregate exceptions and simply returns an instance of 
[UnrecognizedExceptionOccurredException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/UnrecognizedExceptionOccurredException.java).

To enable reporting of aggregated messages, MapMate needs to be made aware of the validation exception (the exception 
class it needs to recognize as validation error). Assuming one has a single ValidationException somewhere in the domain
that is thrown in the factory methods, in case the input is not valid, the MapMate configuration looks like:

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

You can further customize the message of this error by giving in a lambda that maps your validation exception to an 
instance of a 
[ValidationError](../core/src/main/java/com/envimate/mapmate/deserialization/validation/ValidationError.java):

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

Web(service) frameworks usually offer a way to register global exception handlers that map an exception into a response.
This is the place where you register a mapper that generates a response using the instance of
[AggregatedValidationException](../core/src/main/java/com/envimate/mapmate/deserialization/validation/AggregatedValidationException.java).


## Recipes
In the real world, a good recipe provides instructions and ingredients on how to cook stuff, pancakes, for instance.
If you've had some friends over for a pancake party, and you've made good pancakes, chances are, that you are asked
for a recipe. What happens if you share it is essentially making a copy of the recipe. Some IT books take the same approach; most of them can be identified by `book.name.contains("cookbook")`. The problem with that approach
becomes apparent when you update the ingredients or baking temperatures or both - you need to ship a new book and your
clients have to copy&paste the updated recipe again.
 
To avoid that problem for MapMate, we ship with the `Recipe` interface, which allows everyone to craft tasty
MapMate recipes and share them as code. This way, you can change the recipe and roll out the change using your favourite
distribution management - e.g. a versioned maven artifact. 
Recipes allow MapMate to offer support for many different use cases, without polluting the builder interface as well.

In other words, Recipes are little MapMate plugins for some common usecases such as (de)serialization of 
numeric data types or the configuration of Jackson and it's registration as Marshaller in MapMate. They are also an excellent 
opportunity to contribute to MapMate or to implement conventions across multiple projects of multiple teams.

### Using Recipes
Using a recipe is very simple and straight forward. Just call the `usingRecipe` with an instance of your recipe. Let's
showcase that by listing and explaining the recipes MapMate is shipping with.

#### Jackson configuration support
Since we are using Jackson as Marshaller in our projects, we ship with a Recipe to make Jackson work with MapMate with
a few lines of code. It configures Jackson not to attempt to parse numbers or booleans and instead just use the 
string value as stated in [Concepts](Concepts.md) under `String representation`. It also configures it not to serialize properties
with a value of `null`.

Using the 
[JacksonMarshaller](../core/src/main/java/com/envimate/mapmate/builder/recipes/marshallers/jackson/JacksonMarshaller.java)
Recipe is straight forward:
```java
MapMate.aMapMate()
    //...
    .usingRecipe(jacksonMarshallerJson(new ObjectMapper()))
    //...
    .build();
```
You can pass a new instance of ObjectMapper like in the example above, pass your applications instance or pass an even
further customized instance.

#### Support for language primitives (double, int, float, String, ...)
Although we put much effort into stating that we discourage the use of primitives shipped with the language, we 
understand that sometimes things are different and for these times, MapMate ships with a recipe that makes it map
your Serialized Objects even if they contain built-in primitives.

(That is not because we think there are times when Custom Primitives are not the preferred solution, but because we 
believe that a framework should be a slave to your code and not the other way around.) 

Using the [BuiltInPrimitiveSerializedAsStringSupport](../core/src/main/java/com/envimate/mapmate/builder/recipes/primitives/BuiltInPrimitiveSerializedAsStringSupport.java) 
Recipe is straight forward:
```java
MapMate.aMapMate()
    //...
    .usingRecipe(builtInPrimitiveSerializedAsStringSupport())
    //...
    .build();
```

Check out [WithPrimitivesBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/lowlevel/withPrimitives/WithPrimitivesBuilderTest.java)
for a detailed example. 

#### Support for manually registered types
Scanning the classpath and analysing which classes are 
Custom Primitives, which are Serialized Objects and which are to ignore is a great way to trade development effort with
CPU effort. However, if you intend to run on a serverless platform like AWS Lambda, chances are high, that your project 
only contains a handful of Custom Primitives/Serialized Objects and you want a high-speed, optimized application startup.

Another reason to manually define which Objects are allowed to enter and/or leave your service is security. 

Yet another reason would be to make MapMate work with a specifically unconventional Custom Primitive or 
Serialized Object.

In these cases or if you are just a control freak, the 
[ManualRegistry](../core/src/main/java/com/envimate/mapmate/builder/recipes/manualregistry/ManualRegistry.java) 
is your recipe of choice.

Control/Security freaks and lazy fancy Lambdas will find it's usage straight forward:
```java
MapMate.aMapMate()
    .usingRecipe(manuallyRegisteredTypes()
            .withSerializedObjects(
                    com.envimate.mapmate.builder.models.conventional.Email.class
            )
            .withCustomPrimitives(
                    com.envimate.mapmate.builder.models.conventional.EmailAddress.class,
                    com.envimate.mapmate.builder.models.conventional.Subject.class,
                    com.envimate.mapmate.builder.models.conventional.Body.class)
    )
    //...
    .build();
```

Teaching MapMate how to work with unconventional types is a bit more complex and requires a basic understanding
of how MapMate is interacting with your types.

MapMate is behaving like you'd behave when interacting with Custom Primitive: it's calling one of its methods to obtain
a String or passes a string into a factory method when creating a Custom Primitive. Both of the actions can be broken 
down to 3 simple pieces of information: the type, a method that converts the type into a string and a method that 
converts the string into the type. Given that information, manually registering an unconventional Custom Primitive becomes
straight forward:

```java
MapMate.aMapMate()
                .usingRecipe(manuallyRegisteredTypes()
                        .withCustomPrimitive(EmailAddress.class, EmailAddress::serialize, EmailAddress::deserialize)
                        .withCustomPrimitive(Subject.class, Subject::serialize, Subject::deserialize)
                        .withCustomPrimitive(customConventionBody, Body::serialize, Body::deserialize)
                )
                //...
                .build();
```

Instead of passing the type and 2 functions as parameters, you can also provide and instance of
[CustomPrimitiveDefinition](../core/src/main/java/com/envimate/mapmate/builder/definitions/CustomPrimitiveDefinition.java).

Serialized Objects are a bit more complicated to deal with, and we admit that there is still some simplicity to gain by 
enhancing the code. Since that is going to be a bit of effort, we decided to wait until we get more feedback and 
use cases from our users so that we can put in the effort where it brings the most benefit. With that in mind,
let's dive into the Lion's Den.

The "easy" way to help MapMate understand how to deal with an unconventional Serialized Object is to provide the type,
a list of fields that are supposed to be serialized and a string representing the factory methods name:
```java
MapMate.aMapMate()
                .usingRecipe(manuallyRegisteredTypes()
                        .withSerializedObject(Email.class, Email.class.getFields(), "restore")
                )
                //...
                .build();
```

See [IndividuallyAddedModelsBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/IndividuallyAddedModelsBuilderTest.java) 
for more details.

If you want to dig even deeper read on. MapMate is using the 
[DeserializationDTOMethod](../core/src/main/java/com/envimate/mapmate/deserialization/methods/DeserializationDTOMethod.java)
interface to deserialize Serialized Objects:

```java
    Object deserialize(Class<?> targetType, Map<String, Object> elements) throws Exception;

    Map<String, Class<?>> elements(Class<?> targetType);
```

The method `elements` has to provide a Map representing the factory method's parameter list for a given type.
Examining the [Email](../core/src/test/java/com/envimate/mapmate/builder/models/conventional/Email.java),
as an example for a Serialized Object, will provide some help understanding what that sentence means.
To create an instance of Email, MapMate needs the `elements`:

```json
{
  "sender": "EmailAddress.class",
  "receiver": "EmailAddress.class",
  "subject": "Subject.class",
  "body": "Body.class"
}
```

Once MapMate obtained all of the elements, it calls `deserialize` with the target type and a Map similar to 
the one provided by `elements`, but with actual values instead of types.

For serialization, MapMate is using the 
[SerializationDTOMethod](../core/src/main/java/com/envimate/mapmate/serialization/methods/SerializationDTOMethod.java)
interface. At this point, it carries too many internals, it is hard to understand and even harder to explain. If
you want/need to provide your own implementation, check out 
[SerializedObjectDefinition](../core/src/main/java/com/envimate/mapmate/builder/definitions/SerializedObjectDefinition.java)
and read your way into the code.

### Crafting your own Recipes
To create a recipe, one has to understand the 
[Recipe interface](../core/src/main/java/com/envimate/mapmate/builder/recipes/Recipe.java) and the process of how a 
MapMate instance is built by the MapMateBuilder.

### The Builder Process
One of the goals of MapMate is to be ultra customizable. Another one is ultra short and straightforward conventions to reduce 
configuration effort. To satisfy both of the goals, MapMate is built using a builder that implements the 
following process:

1. Allow all recipes to interact with the Builder itself by calling the `cook` method with the builder instance.
2. Allow all recipes to provide Custom Primitive and Serialized Object definitions.
3. Ask the `PackageScanner` to list all detection candidates a.k.a. the classes that might be a Custom Primitive, 
Serialized Object, or something else, MapMate is not interested in.
4. Remove all known Custom Primitives and Serialized Objects (obtained in Step 2) from that list.
5. Use the `Detector` to obtain Custom Primitive and Serialized Object definitions from detection candidates.
6. Wrap up everything else and build the MapMate instance.

Check out the code of [MapMateBuilder](../core/src/main/java/com/envimate/mapmate/builder/MapMateBuilder.java) for more
little nifty details.
 
### Understanding the Recipe Interface 

```java
public interface Recipe {
    default void cook(final MapMateBuilder mapMateBuilder) {
    }

    default Map<Class<?>, CustomPrimitiveDefinition> customPrimitiveDefinitions() {
        return Map.of();
    }

    default Map<Class<?>, SerializedObjectDefinition> serializedObjectDefinitions() {
        return Map.of();
    }
}
```

The first thing to note is that the interface only contains methods, which implementation made is optional by
providing a NOOP default implementation.

The `cook` method is quite simple; it's essentially a call back with the MapMateBuilder instance that can be used to wrap
multiple builder calls into a single Recipe. The
[JacksonMarshaller](../core/src/main/java/com/envimate/mapmate/builder/recipes/marshallers/jackson/JacksonMarshaller.java)
is a great example of Recipes implementing only that method.

`customPrimitiveDefinitions` and `serializedObjectDefinitions` allows Recipes to provide Custom Primitive and 
Serialized Object Definitions. The 
[ManualRegistry](../core/src/main/java/com/envimate/mapmate/builder/recipes/manualregistry/ManualRegistry.java)
is the example providing with more insights as to how these methods can be used.  

We'll be happy to receive your requests for new Recipes and contributions!

## FAQ

**Q: Do I have to provide both serialization and deserialization methods for my Custom Primitives and Serialized Objects?**

_A: No, you can have classes that contain only serialization or only deserialization methods. Examples of these are 
Request objects being only **deserialized** and Response objects only **serialized**_

