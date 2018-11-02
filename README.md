[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.envimate/mapmate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.envimate/mapmate)

<img src="mapmate-logo.png" width="400px" />
# Mapmate
A library that helps you (de)serialize your DDD domains.
Available in the official [Maven Repository](https://mvnrepository.com/artifact/com.envimate/mapmate)

### Preface

### Table of Content
- [Preface](#markdown-header-preface)
- [Table of content](#markdown-header-table-of-content)
- [Highlighted features](#markdown-header-highlighted-features)
- [Getting Started](#markdown-header-getting-started)
- [Concepts](#markdown-header-concepts) 
- [Tutorial](#markdown-header-tutorial)
    - Serializing & Deserializing
    - Validation
- [Integration Showcase](#markdown-header-integration-showcase)
    - JSON using Gson
    - JSON using Jackson
    - YAML using Jackson
    - XML using XStreamer
- [Advanced](#markdown-header-advanced)
    - Advanced builder configuration
    - Configuring adapters
    - Advanced validation
    - Injectors

### Highlighted features 
Some features MapMate offers:

 -  **Debuggable** 
    - _MapMate stays clear of magic, meaning debugging is easy and straight-forward._
 - **Support for validating your domain**
    - _MapMate allows you to check for validation exceptions and aggregates them accordingly._
    - _You will know exactly what field of what domain was faulty._ 
    - _MapMate also offers ways of detecting redundant validation exceptions._
 - **Non-intrusive usage and configuration** 
    - _MapMate uses modern Java to impose as little of it as possible, staying clear of your production code._
 - **Highly configurable** 
    - _MapMate is highly configurable, allowing configuration for interception, custom (de)serialization, validations and more. Making MapMate whatever you need it to be._
    - _Configuration is done using self-explanatory builders, helping you configure it every step of the way.

### Getting started
To start using mapmate you can add a dependency in your maven project
```
<dependency>
    <groupId>com.envimate</groupId>
    <artifactId>mapmate</artifactId>
    <version>latest</version>
</dependency>
```
MapMate does require to be compiled using parameter names. This results in some configuration in your `pom.xml` and your IDE of choice by setting the -parameters flag of your jdk.
For maven this can be done using the `maven-compiler-plugin`.
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
For your IDE it's as simple as having it compile using the `-parameters` command-line argument. 

### Concepts
In domain driven design there are conventions around how your domain should be structured.
One of these conventions is that everything has to be made a type to avoid 'primitive obsession'.
An example of this is that the `email` field is not of type *String*, but of type *Email* with possibly an internal private String value. 

When constructing DDD domains, a problem many face is how to properly (de)serialize such a datastructure. This is where MapMate aims to help.

MapMate differentiates between *data transfer objects* and *custom primitives*. A data transfer object is an aggregation of custom primitives.
For example a *Person* is a data transfer object, which contains a custom primitive *FirstName*. During configuration you'll tell or help MapMate find these types.
 
During configuration MapMate forms a list of *definitions*. A definition is a known type within your domain which is either a data transfer object or a custom primitive.
When (de)serializing, MapMate checks if it knows the associated type, and applies the correct (de)serialization as per configuration.
This approach allows for no reflection during (de)serialization, and little reflection during configuration.

MapMate typically marshals originalInput to a Java Map/List to start of from. Configuration allows you to use different marshallers; i.e. Gson, Jackson, Yaml or DocumentBuilder.

Various ways are provided to intervene with the (de)serialization process, enabling you to divert from default behaviours or enrich potentional originalInput/output.

### Tutorial
Here's an example domain as you would find in a typical DDD project; consisting of a *User* with a *UserName* and *EmailAddress*.

**User.class**
```
public final class User {
    public UserName userName;
    public EmailAddress emailAddress;

    private User(final UserName userName, final EmailAddress emailAddress) {
        this.userName = userName;
        this.emailAddress = emailAddress;
    }

    public static User from(final UserName userName, final EmailAddress emailAddress) {
        return new User(userName, emailAddress);
    }
}
```
**UserName.class**
```
public final class UserName {
    private final String value;

    private UserName(final String value) {
        this.value = value;
    }

    public String internalValue() {
        return this.value;
    }

    public static UserName fromString(final String value) {
        return new UserName(value);
    }
}
```
**EmailAddress.class**
```
public final class EmailAddress {
    private final String value;

    private EmailAddress(final String value) {
        this.value = value;
    }

    public String internalValue() {
        return this.value;
    }

    public static EmailAddress fromString(final String value) {
        return new EmailAddress(value);
    }
}
```
##### Serializing & Deserializing
Marshalling this domain to for example JSON would likely be inconvenient using traditional libraries. 
And we don't want to omit DDD conventions, just to allow for marhsalling. 
Enter Mapmate; with a minimal configuration one can easily serialize these domains to JSON as we would expect it.

This is how a domain is serialized:
```
public static void main(String [] args) {
    User user = User.from(
        UserName.fromString("johndoe"),
        EmailAddress.fromString("johndoe@example.com"));

    Serializer serializer = aSerializer()
        .withMarshaller(new Gson()::toJson)
        .thatScansThePackage("your.project.domain")
            .forCustomPrimitives()
                .forTypes(allTypes())
                    .serialized()
                        .usingMethodNamed("internalValue")
        .thatScansThePackage("your.project.domain")
            .forDataTransferObjects()
                .forTypes(allTypes())
                    .serialized()
                        .bySerializingItsPublicFields()
        .build();

    String output = serializer.serialize(user);
    //output: {"userName": "johndoe", "emailAddress": "johndoe@example.com"}
}
```
Notice how the output JSON only contains actual primitives, instead of your custom primitives.

Now here is how we deserialize an originalInput JSON:
```
public static void main(String [] args) {
    String originalInput = "{\"userName\": \"johndoe\", \"emailAddress\": \"johndoe@example.com\"}";
   
    Deserializer deserializer = aDeserializer()
        .withUnmarshaller(new Gson()::fromJson)
        .thatScansThePackage("your.project.domain")
            .forCustomPrimitives()
                .forTypes(allTypes())
                    .deserialized()
                        .usingAMethodNamed("fromString")
        .thatScansThePackage("your.project.domain")
            .forDataTransferObjects()
                .forTypes(allTypes())
                    .deserialized()
                        .usingTheSingleFactoryMethod()
        .build();

    User fromJson = deserializer.deserialize(output, User.class);
}
```
This is the simplest form of (de)serializing a domain to and from JSON. The configured builder will take care of any and all reflection and can be injected with your favored injection library.

Also note that we - the programmer - supply the underlying marshaller, in this case it's Gson using `.withMarshaller(new Gson()::toJson)`.
This enables us to use MapMate in combination with any marshaller; i.e. XML or YAML.

In the configuration you'll see that we configure the (de)serializer to look for custom primitives. In this case it'll identify them by the method called 'internalValue'. 
Aftwards we identify data transfer objects by the existence of a factory method. If these conflict, MapMate will let you know by throwing an exception.
Ofcourse, this behaviour can be steered with greater accuracy using a proper class filter, shown in this document below.

##### Validation
A big part of DDD is the validation of your domain on creation. To add onto our scenario we could have the EmailAddress validate it's value to be a valid email address.

**EmailAddress.class**
```
public final class EmailAddress {
    private final String value;

    private EmailAddress(final String value) {
        this.value = value;
    }

    public String internalValue() {
        return this.value;
    }

    public static EmailAddress fromString(final String value) {
        if(isValidEmailAddress(value)) {
            return emailAddress
        } else {
            throw InvalidEmailAddressException.fromValue(value);
        }
        return new EmailAddress(value);
    }
    
    ...
}
```
**InvalidEmailAddressException.class**
```
public final class InvalidEmailAddressException extends RuntimeException {
    private InvalidEmailAddressException(final String msg) {
        super(msg);
    }

    public static InvalidEmailAddressException fromValue(final String value) {
        String msg = String.format("invalid email address %s", value);
        return new InvalidEmailAddressException(msg);
    }
}
```
MapMate asks for a clear boundry between just any exception, and validation errors. Adding the following line to MapMates configuration helps MapMate decide which exception is a validation error and which is not.

```
aDeserializer()
  ...
  .mappingExceptionToValidationError(InvalidEmailAddressException.class, t -> new ValidationException(t.getMessage()))
  ...
  .build()
```

Mapmate will track each recognized validation error thrown and return all of them back to you; you can choose to catch it for a more tailored approach. For example you can get the underlying exception, where the exception is thrown and who it is blaming.
```
try {
    deserializer.deserialize(json, User.class);
} catch(final AggregatedValidationException e) {
    e.validationErrors.forEach(error -> {
        System.out.println(error.getException()); // The exception thrown
        System.out.println(error.getFrom()); // Where it was thrown
        System.out.println(error.getBlamed()); // What fields it is blaming
    });
}
```
The *AggregatedValidationException* collects all recognized exceptions thrown during deserialization and uses 'positions' to tell you where the exceptions were thrown. In this simple case `.getFrom()` would return `"emailAddress"`. But this could well be a more descriptive position in more complex domains, i.e. `"users.[2].address.streetName"`. The root of the json document is referred to as `""`.

###Integration Showcase
Configuring a (un)marshaller is done using the `.withMarshaller(Marshaller)` and `.withUnmarshaller(Unmarshaller)` in their respective configuration.
This allows MapMate to parse however, and to whatever syntax you'd like. Below are four tested examples.
#####JSON using Gson
```
aDeserializer()
    .withUnmarshaller(new Gson()::fromJson)
    ...

```
#####JSON using Jackson
```
aDeserializer()
    .withUnmarshaller(new Unmarshaller() {
        @Override
        public <T> T unmarshal(String originalInput, Class<T> type) {
            try {
                ObjectMapper mapper = new ObjectMapper(new JSONFactory());
                return mapper.readValue(originalInput, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    })
    ...
```
#####YAML using Jackson
```
aDeserializer()
    .withUnmarshaller(new Unmarshaller() {
        @Override
        public <T> T unmarshal(String originalInput, Class<T> type) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                return mapper.readValue(originalInput, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    })
    ...
```
#####XML using XStream
```
aDeserializer()
    .withUnmarshaller(new Unmarshaller() {
        @Override
        public <T> T unmarshal(String originalInput, Class<T> type) {
            XStream xStream = new XStream(new DomDriver());
            xStream.alias("root", Map.class);
            return (T) xStream.fromXML(originalInput, type);
        }
    })
    ...
```

#Advanced
###Advanced builder configuration
The (de)serializer builders have many options available to the user to further adapt MapMate to your needs.
##### Package scanning
The way MapMate identifies definitions can greatly be influenced, using class filters. 
An example of a filter:
```
aDeserializer()
    .thatScansThePackage("your.project.domain")
        .forCustomPrimitives()
            .forTypes(allTypes())
```
We specifically tell the builder to scan for custom primitives, and that we're including all classes.
'allTypes()' in truth is a pre-made filter, but you can put any implementation of a ClassFilter there.
```
aDeserializer()
    .thatScansThePackage("your.project.domain")
        .forDataTransferObjects()
            .forTypes(type -> type.getSimpleName().endsWith("DTO"))
```
Here we show that we'll match the suffix of the class to be 'DTO'. Any filtering method we provide for you is implemented using such filters.
Convenient filtering method are:
```
aDeserializer()
    .thatScansThePackage("your.project.domain")
        .forDataTransferObjects()
            .identifiedByClassNamePrefix("DTO")
            .identifiedByClassNameSuffix("DTO")
            .excluding(Person.class)
```
Once the filters are set up one can select the method of (de)serialization. Depending on the builder there are several options available.

| Builder | Type | Method | Description |
| --- | --- | --- | --- |
|Serialization|Custom primitive|`.usingAMethodNamed(String)`|Uses function returning a String with the given name.|
|Serialization|Data transfer object|`.bySerializingItsPublicFields()`|Serializes by serializing all its public fields.|
|Deserialization|Custom primitive|`.usingAMethodNamed(String)`|Uses function by the given name, returning an instance of itself given a String| 
|Deserialization|Data transfer object|`.usingTheSingleFactoryMethod()`|Scans for a single factory method|
|Deserialization|Data transfer object|`.usingTheFactoryMethodNamed(String)`|Scan for a factory method with the given name, in case multiple exist|
|Serialization|Both|`.using(SerializationMethod)`|Providing your implementation of its serialization.|
|Deserialization|Both|`.using(DeserializationMethod)`|Providing your implementation of its deserialization.|

##### Single definitions
Besides package scanning, the builders also provide singular definition configuration. I.e.:
```
    .withDataTransferObject(Person.class)
        .deserializedUsingSingleFactoryMethod()
        .deserializedUsingAdapter(DeserializerAdapter)    
        .deserializedUsing(DeserializationMethod)
    .withCustomPrimitive(FirstName.class)
        .deserializedUsingStaticMethod(Function<String, ?>)
        .deserializedUsingMethodWithName(String)
        .deserializedUsing(DeserializationMethod)
```
Similar options are available for Serialization.

Beware that this does not override an existing definition from the same type. Make sure to use `.excluding(Class<?>)` during package scanning where needed, before adding single definitions.
If the builder has two definitions with the same type, it will throw an exception.
###Configuring adapters
Adapters are mainly used when a domain is too complex for us to deserialize. In DDD the usage of Java's Collections framework inside your domain is often discouraged; but should you require it, MapMate will not be able to recognize associated types as a valid definition.
In order to have the definition anyway, an adapter can be used, in which you tell MapMate how to deserialize. 
```
    .withDataTransferObject(Person.class)
        .deserializedUsingAdapter((originalInput, targetType, deserializer, exceptionTracker) -> {
            ...
        }
``` 
**originalInput** - the originalInput as Map.
**targetType** - the type of the expected output.
**deserializer** - a pointer to the deserializer instance.
**exceptionTracker** - a helper to help you track exceptions.
It is advised to only help the deserializer minimally, using the given *deserializer* to further deserialize recursive parts.
An example of a fully implemented adapter for a DTO containing an ArrayList field.
```
    .withDataTransferObject(Person.class)
        .deserializedUsingAdapter((originalInput, targetType, deserializer, exceptionTracker) -> {
            final FirstName firstName = deserializer.deserialize(
                    originalInput.get("firstName"), 
                    FirstName.class, 
                    exceptionTracker.stepInto("firstName"));
            final LastName lastName = deserializer.deserialize(
                    originalInput.get("lastName"), 
                    LastName.class, 
                    exceptionTracker.stepInto("lastName"));
            final ArrayList<MiddleName> middleNames = new ArrayList<>();
            
            final ArrayList<String> inputMiddleNames = (List) originalInput.get("middleNames");
            for(String inputMiddleName : originalInput.get("middleNames")) {
                middleNames.add(MiddleName.fromString(inputMiddleName))
            }
            return Person.withName(firstName, middleNames, lastName);
        })
```   
In the above example you can see that we only manually deserialize the collection of middle names. MapMate is perfectly capable of deserializing the first and last name fields.
This also assures you that any validation exceptions are caught and tracked. 

If this adapter would throw an exception, this would become tracked. If you want a finer control of telling where and why this exception was thrown in your adapter; use the `track(Throwable)` and `stepInto(String)` functions of the exception tracker.

###Advanced validation

MapMate returns you all exceptions that are thrown during deserialization. Working with a simple domain often doesn't require any additional exception configuration.
However, sometimes some extra effort is required when you want to avoid having returned a large stacktrace.

In a previous example we've discussed the invalid email scenarion. 
Let's add onto that scenario by expanding the user class, so both the EmailAddress is validated, and the user cannot have a null email address.

**User.class**
```
public final class User {
    public UserName userName;
    public EmailAddress emailAddress;

    private User(final UserName userName, final EmailAddress emailAddress) {}

    public static User from(final UserName userName, final EmailAddress emailAddress) {
        if(emailAddress == null) {
            throw new NullEmailAddressException();
        }
        return new User(userName, emailAddress);
    }
}
```
**EmailAddress.class**
```
public final class EmailAddress {
    private final String value;

    private EmailAddress(final String value) {
        this.value = value;
    }

    public String internalValue() {
        return this.value;
    }

    public static EmailAddress fromString(final String value) {
        final EmailAddress emailAddress = new EmailAddress(value);
        if(emailAddress.isValidEmailAddress()) {
            return emailAddress
        } else {
            throw InvalidEmailAddressException.fromValue(value);
        }
    }
}
```
Now, when we deserialize i.e. `{"userName":"John", "emailAddress":"some.invalid.value"}` we'll get returned two exceptions.
One for the email address being invalid, and one for the user being invalid due to a null email address. Obviously, one caused the other. 
Preferable we'd only get the one exceptions that caused the cascaded exception.

Without additional configuration, the returned exceptions will always blame themselves for an exception. So we can't really tell it's cascaded by something else.
But if we tell MapMate the NullEmailAddressException is linked to the email address field, we might be better able to tell whether something got cascaded or not.
 
Internally MapMate wraps exceptions in a ValidationException.class to keep track of where it is thrown, and who it is blaming.
To solve this, we're gonna help MapMate wrap the exception.
In this case it could simply look like:
```
    .mappingExceptionToValidationError(NullEmailAddressException.class, t -> {
        return new ValidationException(t.getMessage(), "emailAddress");
    })
```
An even better approach would be read the values from the exception itself, i.e.:
```
    .mappingExceptionToValidationError(NullEmailAddressException.class, t -> {
        final NullEmailAddressException e = (NullEmailAddressException) t;
        return new ValidationException(e.getMessage(), e.getBlamedField());
    })
``` 
Now when we run this again, we'll only get the one InvalidEmailAddressException. 

Note that one mapping can only blame one field. Wanting a validation mapping to blame multiple fields is a good indication that it's not validation, but rather business logic.

###Injectors
Injectors are purely for intercepting originalInput and output, and injecting or changing values before (de)serialization.
Using our scenario, imagine our originalInput json is missing a first name: `{"emailAddress": "some@email.com"}`
And we wan't to give it a default name 'anon' - or get the name some other way. We could do something like:
```
    final String json = "{\"emailAddress\": \"some@email.com\"}"
    final User user = deserializer.deserialize(json, User.class, interim -> {
        interim.put("firstName", "anon");
        return interim;
    });
```
Obviously, this example isn't quite that useful since we're writing our own JSON right there. But imagine the possibilities.
The same is possible when serializing.
