# Basic Usage

Once you got hold of a MapMate instance you can then perform:
 
## Serializing to JSON
<!---[CodeSnippet](serializeToJson)-->
```java
final String json = mapMate.serializeToJson(EMAIL);
System.out.println(json);
```

## Deserializing from JSON
<!---[CodeSnippet](deserializeJson)-->
```java
final Email deserializedEmail = mapMate.deserializeJson(json, Email.class);
```

## Serializing to YAML
<!---[CodeSnippet](serializeToYaml)-->
```java
final String yaml = mapMate.serializeToYaml(EMAIL);
System.out.println(yaml);
```

## Deserializing from YAML
<!---[CodeSnippet](deserializeYaml)-->
```java
final Email deserializedEmail = mapMate.deserializeYaml(yaml, Email.class);
```

## Serializing to XML
<!---[CodeSnippet](serializeToXml)-->
```java
final String xml = mapMate.serializeToXml(EMAIL);
System.out.println(xml);
```

## Deserializing from XML
<!---[CodeSnippet](deserializeXml)-->
```java
final Email deserializedEmail = mapMate.deserializeXml(xml, Email.class);
```

## Serializing to YOUR_CUSTOM_FORMAT
<!---[CodeSnippet](serializeToCustomFormat)-->
```java
final String customFormat = mapMate.serializeTo(EMAIL, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
System.out.println(customFormat);
```

## Deserializing from YOUR_CUSTOM_FORMAT
<!---[CodeSnippet](deserializeCustomFormat)-->
```java
final Email deserializedEmail = mapMate.deserialize(customFormat, Email.class, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
```
