## Getting Started

### Download 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core)
```xml
<dependency>
    <groupId>com.envimate.mapmate</groupId>
    <artifactId>core</artifactId>
    <version>latest</version>
</dependency>
```

[MapMate jar downloads](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core) are available 
from Maven Central.

MapMate uses method parameter names to construct your objects, hence requires you to compile with parameter names.
This is configured by passing the `-parameters` flag to the java compiler.

Maven configuration:
```
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

For your IDE it's as simple as having it compile using the `-parameters` command-line argument of java compiler.

More on the flag you can read in [javac official documentation](https://docs.oracle.com/en/java/javase/12/tools/javac.html) 

### Minimal Configuration

MapMate needs to know the package where your Custom Primitives and Serialized Objects reside. MapMate is also unaware 
of the chosen format, hence needs to be configured with (Un)marshallers to deal with the format conversion.

Here is an example that scans the package `com.envimate.examples` and uses Gson as the (un)marshalling
library.

### Default Conventions Explained

* MapMate respects the access modifiers and does not use any non-public field or method. Ever.
* MapMate scans the given package, visiting every class to identify whether it is a Custom Primitive or a Serialized Object. ([customize this](#packages-and-classes-to-scan))
* A class is considered to be a Custom Primitive if it has a serialization method named "stringValue" and a static
 deserialization method named "fromStringValue".([customize this](#overriding-detector))
* A class is considered to be a Serizlied Object if it has a public static factory method name "deserialize" 
or the name of the class matches one of these patterns
```
.*DTO
.*Dto
.*Request
.*Response
.*State
```
([customize this](#overriding-detector))
* Serialized Objects are serialized using the public fields(key:value) and deserialized using the same public factory method that 
was used to determine the class being a Serialized Object([customize this](#overriding-detector))
* On exception, by default, no aggregation happens and you will receive an instance of "UnrecognizedExceptionOccurredException" ([customize this](#validation-exceptions))

Example of usage of the _Conventional_ MapMate can be found in [`com.envimate.mapmate.builder.ConventionalBuilderTest`](https://github.com/envimate/mapmate/blob/master/core/src/test/java/com/envimate/mapmate/builder/ConventionalBuilderTest.java)
