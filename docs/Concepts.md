* [Concepts](#concepts)
  * [Custom Primitives](#custom-primitives)
  * [Serialized Objects](#serialized-objects)
  * [String Representation](#string-representation)
  * [(Un)marshalling](#unmarshalling)
  * [Validation Errors](#validation-errors)

# Concepts

What we call "Custom primitives and Serialized Objects" comes under many names. Some call it [Value Object](http://wiki.c2.com/?ValueObject) or [Value Java Objects or VALJO](https://blog.joda.org/2014/03/valjos-value-java-objects.html), some call it [Data Value](https://refactoring.guru/replace-data-value-with-object) or [Custom Value Type](https://en.wikipedia.org/wiki/Value_object#Java). You may have also heard about [Project Valhalla](https://en.wikipedia.org/wiki/Project_Valhalla_(Java_language) and Value Types that come with it.  
The definition differs also in terms of whether or not to combine multiple primitive values into one Object, or create classes for each such field. 
Independent of how they are called, and even how the details of definition look like, all of them are there to fight [Primitive Obsession](https://blog.ploeh.dk/2011/05/25/DesignSmellPrimitiveObsession/).

The concept is there for a long time now. Yet a lot of frameworks that have become "industry standards" rely on [JavaBeans style objects]((http://www.javapractices.com/topic/TopicAction.do?Id=84)) and offer little to none support for this concept.

We think that creating your own classes and [keeping the validation logic inside the class](https://enterprisecraftsmanship.com/2017/08/07/always-valid-vs-not-always-valid-domain-model/), makes the code cleaner, more readable, [less dependant on the framework](https://blog.cleancoder.com/uncle-bob/2014/05/11/FrameworkBound.html) being used and most of all [safer](articles/DomainDrivenSecurity.md). 

On this page we'd like to define what we call Custom Primitives and Serialized Objects, and which conventions we use to declare them. 

## Custom Primitives

are in a way, extending java with your domain's language. They represent only a single value and contain a factory 
method, that validates the value and only after that creates the instance.

Examples of custom primitive are: an email address, a zipcode, a first name, an amount, a price, a currency, an ID, etc. 

List of properties, that describe a Custom Primitive:

* Represents one logical property
* Immutable, which implies
    * final modifier on the class
    * the field being private and final
    * no setters
* equals and hashCode implemented 
* Only private Constructors to enforce the use of factory methods that perform validation 
* Factory method (in our default convention called "fromStringValue") that validates and creates the object from a String
* A method that returns the String representation of the value for serialization (conventional method name - "stringValue")

## Serialized Objects

is anything in your domain that you aim to Serialize at some point. Anything you need to transport over the wire, save in the database, print to the console, etc. 

These are constructed using Custom Primitives, or other Serialized Objects and are essentially a [composite data type](https://en.wikipedia.org/wiki/Composite_data_type) except it's build with your domain's Custom Primitives. 
You can also think of it as a "bag of custom primitives or serialized objects", a combination of those that aims to represent data. 

Examples of serialized objects are: DTOs: Request/Response objects, an Order, an Email, a Receipt, or just a Name consisting of FirstName and LastName, etc.     

List of properties, that describe a Serialized Object:

* No primitive fields, all fields should be Custom Primitives, or other Serialized Objects
* All the fields that participate in the serialization are public
* Immutable
* Only Private Constructors
* At least one Factory Method (default convention - "deserialize") that takes all the public fields, performs validation of their combination and constructs the object instance.
* equals and hashCode


Here are [some thoughts in this direction by other people](http://blog.cleancoder.com/uncle-bob/2019/06/16/ObjectsAndDataStructures.html)

## String representation

The most generic form of representation that is both human-friendly and serialization friendly is String. You use your objects, to transport information from one service to another, from server to client, from server to database, from client back to server, or even from screen to paper or paper to screen. 

What you don't want, is repeatedly verifying(doubting), whether the String that comes to your application, is valid or not, is it really an email or not, can it be converted to an Integer, when you expect a number or not.

So you keep your validation and construction logic _inside_ the object and provide methods for converting it back to the transportation mechanism (a.k.a String).

Hence our default convention is to call the "serialization" and "deserialization" methods for Custom Primitives **stringValue** and **fromStringValue**, because you need to get _the string value_ of the object to transport/persist/serialize it, and you can then deserialize your Custom Primitive _from a string value_. As described in details in the [User Guide](UserGuide.md) you can modify those conventions or even ship your own if you so wish.

This approach also means MapMate expects that the Unmarshaller does not attempt to guess and parse number literals into specific types. The parser's guesstimate might or might not be correct and even features targeting that problem (e.g. [USE_BIG_DECIMAL_FOR_FLOATS, USE_BIG_INTEGER_FOR_INTS](https://github.com/FasterXML/jackson-databind/wiki/Deserialization-Features)) don't cover all possible scenarios.
Having this in mind, we propose centralizing the parsing and validation of the Custom Primitive, at the same time giving the control to you. Validate once, use everywhere.

## (Un)marshalling

In order to support multiple formats like JSON, XML, YAML, etc Serialized Objects are converted into Maps of Maps and Strings. This map is respectively deserialized from a specific format, or serialized into that format. The process of serializing that Map into a format, is what we call [Marshalling](https://en.wikipedia.org/wiki/Marshalling_(computer_science)) and the reverse operation Unmrashalling.
MapMate does not ship with any marshallers, rather an interface that allows MapMate to delegate this task to existing frameworks.
Examples of these frameworks include [Gson](https://github.com/google/gson), [Jackson](https://github.com/FasterXML/jackson), [X-Stream](https://x-stream.github.io/).

## Validation Errors

Typically, when used in a web(service) framework context, MapMate is acting as a Request/Response (de)serialization framework. The validation of the request then, is expected to return a clear message to the caller, about the occurred validation errors. In case of multiple validation errors, communicating those one-by-one, especially in case of a UI, does not make sense to us. Hence, we have implemented a built-in aggregation of validation errors. These are based on the validation exception class provided by you during the configuration of MapMate instance. The [ValidationError](../core/src/main/java/com/envimate/mapmate/deserialization/validation/ValidationError.java) is then constructed, whenever the instance of that exception is thrown, and the message returned contains the dot-notation "path" to the invalid field.

## Injections

Injections allow to enrich the original serialized information with contextual data that is available when a factory 
method of any Serialized Object is called. The intended use case for this feature is to allow web frameworks to map
authentication headers (JWT token) into actual AuthenticatedUser instances and inject them into the request DTO.

Example: Update a users shipping address.
The request json would look like this:
```json
{
  "street": "John Doe Street 23",
  "zip": "234223",
  "city": "Jane Doe"
}
```
The DTO may look like this:
```java
public final class UpdateShippingAddressRequest {
    public final AuthenticatedUser authenticatedUser;
    public final Street street;
    public final Zip zip;
    public final City city;
    //...
    public static UpdateShippingAddressRequest deserialize(
                        final AuthenticatedUser authenticatedUser,
                        final Street street,
                        final Zip zip,
                        final City city) {
        //...
    }
    //...
}
```
The Use Case would look like this:
```java
public UpdateShippingAddressResult updateShippingAddress(final UpdateShippingAddressRequest request) {
    //...
    final User user = this.userRepository.byId(request.authenticatedUser.id);
    user.updateShippingAddress(request.street, request.zip, request.city);
    this.userRepository.update(user);
    //...
}
```
 
Outside of UI context, this aids creating descriptive log entries, that could be used as a means of reproducing and tracing the errors.  
