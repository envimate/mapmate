# Detection

### Package Scanning
MapMate ships with a package scanner that scans the list of packages it's been configured to scan for 
Custom Primitives and Serialized Objects. MapMate has a [default convention](#default-conventions-explained) of how to 
identify these special classes. In this section, we describe how to control which classes participate in the detection.

The builder provides the possibility to register a list of package names, that are scanned _recursively_:

<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/PackageScannerExample.java config)-->
```java
final MapMate mapMate = MapMate.aMapMate(PACKAGE_TO_SCAN_1, PACKAGE_TO_SCAN_2 /* etc.*/)
        /* further configuration */
        .build();
```

#### PackageScanner
The builder of MapMate accepts a 
[PackageScanner](../core/src/main/java/com/envimate/mapmate/builder/PackageScanner.java) as an alternative to the list 
of packages. This interface has a single method `List<Class<?>> scan();` that is responsible for returning classes that
are suspect to being a custom primitive or a serialized object:

<!---[CodeSnippet](core/src/main/java/com/envimate/mapmate/builder/scanning/PackageScanner.java)-->
```java
public interface PackageScanner {
    List<Class<?>> scan();
mmm
}
``` 

#### Whitelisting and Blacklisting Packages and Classes
You might want to control which packages are scanned by MapMate to reduce startup times or for other reasons. The 
[DefaultPackageScanner](../core/src/main/java/com/envimate/mapmate/builder/DefaultPackageScanner.java) provides 
factory methods that allow to whitelist or blacklist certain packages and/or classes: 

<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/PackageScannerExample.java api)-->
```java
MapMate.aMapMate(DefaultPackageScanner.defaultPackageScanner(
        List.of(THE_PACKAGE_NAMES_TO_SCAN_RECURSIVELY), // TODO
        List.of(THE_LIST_OF_CLASSES_TO_INCLUDE), // TODO
        List.of(THE_PACKAGE_NAMES_TO_BLACKLIST_RECURSIVELY), // TODO
        List.of(THE_LIST_OF_CLASSES_TO_EXCLUDE)) // TODO
)
        .usingJsonMarshaller(gson::toJson, gson::fromJson)
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

<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/DocExample1.java withoutPackageScanning)-->
```java
final MapMate mapMate = MapMate.aMapMate()
        /* further configuration */
        .build();
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
A class is considered to be a Custom Primitive if it has

a) a serialization method named "stringValue" AND

b) at least one of the following (interpreted in that order):
- a static deserialization method* named "fromStringValue"
- a static deserialization method** whose name contains the class name
- a public constructor that takes exactly one single String argument

*A serialization method is public, returns an instance of String and takes no arguments.

**A deserialization method is 
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
The method "anEmailAddress" is a valid deserialization method, since it's name contains the className. Other valid 
names would be "deserialize", "theEmailAddressWithValue", etc.

#### Default Conventions for Serialized Objects 
A class is considered to be a Serialized Object if it has a public static factory method name "deserialize". 
Alternatively, it is also considered as such if the name of the class matches one of the patterns
```
.*DTO
.*Dto
.*Request
.*Response
.*State
```
AND MapMate can find either a "conventional deserialization method" or, alternatively, a public constructor.

The detection of a "conventional deserialization method" follows this algorithm:

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

The detection of the public constructor follows this algorithm:
1. If the class has a single public constructor -> that's the one
2. if you have multiple public constructors, the one that has all the fields as 
parameters wins

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

<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/DetectorExample.java detector)-->
```java
MapMate.aMapMate("com.envimate.examples")
        .usingJsonMarshaller(new Gson()::toJson, new Gson()::fromJson)
        .withDetector(ConventionalDetectors.conventionalDetector(
                "myCustomPrimitiveSerializationMethodName",
                "myCustomPrimitiveDeserializationMethodName",
                "mySerializedObjectDeserializationMethodName",
                ".*Dto"))
        .build();
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