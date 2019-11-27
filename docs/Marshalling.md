# Marshalling
MapMate is unaware of the format you chose to represent the string value of your objects.
Upon receiving the String input, MapMate first asks the configured (un)marshaller to parse the `String`
into a `Map<String, Object>`, and then operates with that `Map<String, Object>` by mapping its structure onto the
definitions of the custom primitives and serialized objects to create the instance.

On the serialization side of things, MapMate constructs a map of values using the custom primitive and 
serialized object definitions.
This map is then passed on to the configured marshaller which will output the objects in
the chosen format.

## Common marshallers
MapMate ships with integrated marshallers for the common formats Json, XML and YAML as well as
the so-called `x-www-form-urlencoded` (url-encoded) format used in the HTTP protocol.

### Json
MapMate's Json marshaller can be accessed by integrating the following dependency into your project:
```xml
<dependency>
    <groupId>com.envimate.mapmate.integrations</groupId>
    <artifactId>mapmate-json</artifactId>
    <version>${mapmate.version}</version>
</dependency>
```

You can tell you MapMate instance to use it like this:
<!---[CodeSnippet](json)-->
```java
final MapMate mapMate = MapMate.aMapMate(PACKAGE_TO_SCAN)
        .usingRecipe(JsonRecipe.jsonMarshaller())
        .build();
```

### XML
MapMate's XML marshaller can be accessed by integrating the following dependency into your project:

```xml
<dependency>
    <groupId>com.envimate.mapmate.integrations</groupId>
    <artifactId>mapmate-xml</artifactId>
    <version>${mapmate.version}</version>
</dependency>
```

You can tell you MapMate instance to use it like this:
<!---[CodeSnippet](xml)-->
```java
final MapMate mapMate = MapMate.aMapMate(PACKAGE_TO_SCAN)
        .usingRecipe(XmlRecipe.xmlMarshaller())
        .build();
```

### YAML
MapMate's YAML marshaller can be accessed by integrating the following dependency into your project:
```xml
<dependency>
    <groupId>com.envimate.mapmate.integrations</groupId>
    <artifactId>mapmate-yaml</artifactId>
    <version>${mapmate.version}</version>
</dependency>
```
You can tell you MapMate instance to use it like this:

<!---[CodeSnippet](yaml)-->
```java
final MapMate mapMate = MapMate.aMapMate(PACKAGE_TO_SCAN)
        .usingRecipe(yamlMarshaller())
        .build();
```


### application/x-www-form-urlencoded
MapMate's url encoded marshaller is shipped with the `core` package and therefore does not need an additional dependency.
You can add it to your MapMate configuration like this:
<!---[CodeSnippet](urlencoded)-->
```java
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingRecipe(UrlEncodedMarshallerRecipe.urlEncodedMarshaller())
        .build();
```

In order to use it, you need to use the corresponding `MarshallingType`:
<!---[CodeSnippet](urlencodedusage)-->
```java
final String urlEncoded = mapMate.serializeTo(object, UrlEncodedMarshallerRecipe.urlEncoded());
```

## Registering your own marshaller
If these marshallers do not fit your needs, you can easily provide your own.
There are convenience methods to register common marshalling types, such as JSON, XML, YAML.

```java
MapMateBuilder usingJsonMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller)
```

```java
MapMateBuilder usingYamlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller)
```

```java
MapMateBuilder usingXmlMarshallers(final Marshaller marshaller, final Unmarshaller unmarshaller)
```

The marshallers can also be registered in the builder, by providing two maps with the marshalling type as key and an 
instance of the corresponding marshaller as value.

```java
MapMateBuilder usingMarshallers(final Map<MarshallingType, Marshaller> marshallerMap,
                                final Map<MarshallingType, Unmarshaller> unmarshallerMap)
```

In this section, we show the registration of some commonly used marshalling libraries for each of those types. Also,
check out [Jackson configuration support](#jackson-configuration-support) in the recipe collection section.


As you can see the format does not matter, and you can freely provide your Marshalling mechanism, by implementing the
[Marshaller](../core/src/main/java/com/envimate/mapmate/serialization/Marshaller.java) and 
[Unmarshaller](../core/src/main/java/com/envimate/mapmate/deserialization/Unmarshaller.java) interfaces.

We'll be happy to receive contributions to the documentation section here as well, with the typical marshalling 
libraries you use.

### JSON with GSON

Assuming you have a configured instance of `Gson` class, adding it as a JSON Marshaller for MapMate looks like:
<!---[CodeSnippet](jsonWithGson)-->
```java
final Gson gson = new Gson(); // can be further configured depending on your needs.
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingJsonMarshaller(gson::toJson, gson::fromJson)
        .build();
```

### JSON with ObjectMapper
<!---[CodeSnippet](jsonWithObjectMapper)-->
```java
final ObjectMapper objectMapper = new ObjectMapper();
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingJsonMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
        .build();
```


Checkout [ObjectMapperConventionalBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/ObjectMapperConventionalBuilderTest.java) for an example.

### XML with X-Stream
<!---[CodeSnippet](xmlWithXStream)-->
```java
final XStream xStream = new XStream(new DomDriver());
xStream.alias("root", Map.class);

final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingXmlMarshaller(xStream::toXML, new Unmarshaller() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T unmarshal(final String input, final Class<T> type) {
                return (T) xStream.fromXML(input, type);
            }
        })
        .build();
```

Checkout [XmlBuilderTest](../core/src/test/java/com/envimate/mapmate/builder/XmlBuilderTest.java) for an example.

Note: If you wish to marshall in/from XML, don't forget to add the appropriate dependency:

```xml
<dependency>
    <groupId>xstream</groupId>
    <artifactId>xstream</artifactId>
    <version>${xstream.version}</version>
</dependency>
```


### Yaml with ObjectMapper

<!---[CodeSnippet](yamlWithObjectMapper)-->
```java
final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        .usingYamlMarshaller(objectMapper::writeValueAsString, objectMapper::readValue)
        .build();
```

Note: don't forget to add the appropriate dependency to use the YAMLFactory with the ObjectMapper.
```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>${jackson.version}</version>
</dependency>
```

MapMate does not ship with these libraries, so you need to configure the marshaller of your choice also in the dependencies of your project.

### Jackson configuration support
Since we are using Jackson as Marshaller in our projects, we ship with a Recipe to make Jackson work with MapMate with
a few lines of code. It configures Jackson not to attempt to parse numbers or booleans and instead just use the 
string value as stated in [Concepts](Concepts.md) under `String representation`. It also configures it not to serialize properties
with a value of `null`.

Using the 
[JacksonMarshaller](../core/src/main/java/com/envimate/mapmate/builder/recipes/marshallers/jackson/JacksonMarshaller.java)
Recipe is straight forward:

<!---[CodeSnippet](jacksonWithRecipe)-->
```java
final MapMate mapMate = MapMate.aMapMate(YOUR_PACKAGE_TO_SCAN)
        //...
        .usingRecipe(jacksonMarshallerJson(new ObjectMapper()))
        //...
        .build();
```

You can pass a new instance of ObjectMapper like in the example above, pass your applications instance or pass an even
further customized instance.