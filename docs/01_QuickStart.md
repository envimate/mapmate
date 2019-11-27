# Quick Start

## Maven 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core)

```xml
<dependency>
    <groupId>com.envimate.mapmate</groupId>
    <artifactId>core</artifactId>
    <version>${mapmate.version}</version>
</dependency>
```

## Compiler Configuration

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

## Minimal Configuration

MapMate needs to know the package where your [Custom Primitives](Concepts.md#custom-primitives) and [Serialized Objects](Concepts.md#serialized-objects) reside. MapMate is also unaware 
of the chosen format, hence needs to be configured with [(Un)marshaller](Concepts.md#unmarshalling) to deal with the format conversion. 

If you are following the [default conventions](UserGuide.md#default-conventions-explained), and have chosen JSON as format, along with Gson as marshaller, here is the minimal configuration you need to get access to `serializer` and `deserializer` 

<!---[CodeSnippet](instance)-->
```java
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingJsonMarshaller(new Gson()::toJson, new Gson()::fromJson)
        .build();
```

Read the [User Guide](UserGuide.md#configuring-mapmate-instance) for detailed description on how to further configure the MapMate instance.

#### Serialization

Now serializing the object

<!---[CodeSnippet](serialization)-->
```java
final Email email = Email.deserialize(
        EmailAddress.fromStringValue("sender@example.com"),
        EmailAddress.fromStringValue("receiver@example.com"),
        Subject.fromStringValue("Hello"),
        Body.fromStringValue("Hello World!!!")
);

final String json = mapMate.serializeToJson(email);
```

will produce

```json
{
  "receiver": "receiver@example.com",
  "body": "Hello World!!!",
  "sender": "sender@example.com",
  "subject": "Hello"
}
```

#### Deserialization

Using same `mapMate` instance

<!---[CodeSnippet](deserialization)-->
```java
final Email deserializedEmail = mapMate.deserializeJson(json, Email.class);
```

will produce an object equal to `email`.
