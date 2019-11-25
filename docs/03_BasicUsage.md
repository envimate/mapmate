# Basic Usage

Once you got hold of a MapMate instance you can then perform:
 
## Serializing to JSON
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java serializeToJson)-->
```java
final String json = mapMate.serializeToJson(EMAIL);
System.out.println(json);
``` 
## Deserializing from JSON
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java deserializeJson)-->
```java
final Email deserializedEmail = mapMate.deserializeJson(json, Email.class);
``` 
## Serializing to YAML
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java serializeToYaml)-->
```java
final String yaml = mapMate.serializeToYaml(EMAIL);
System.out.println(yaml);
``` 
## Deserializing from YAML
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java deserializeYaml)-->
```java
final Email deserializedEmail = mapMate.deserializeYaml(yaml, Email.class);
``` 
## Serializing to XML
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java serializeToXml)-->
```java
final String xml = mapMate.serializeToXml(EMAIL);
System.out.println(xml);
``` 
## Deserializing from XML
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java deserializeXml)-->
```java
final Email deserializedEmail = mapMate.deserializeXml(xml, Email.class);
``` 
## Serializing to YOUR_CUSTOM_FORMAT
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java serializeToCustomFormat)-->
```java
final String customFormat = mapMate.serializeTo(EMAIL, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
System.out.println(customFormat);
``` 
## Deserializing from YOUR_CUSTOM_FORMAT
<!---[CodeSnippet](core/src/test/java/com/envimate/mapmate/docs/UsageExamples.java deserializeCustomFormat)-->
```java
final Email deserializedEmail = mapMate.deserialize(customFormat, Email.class, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
```
```java
myObject = mapMate.deserialize(myObjectAsSomething, Object.class, MarshallingType.marshallingType("YOUR_CUSTOM_FORMAT"));
``` 