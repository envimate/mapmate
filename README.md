[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.envimate.mapmate/core)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/2894/badge)](https://bestpractices.coreinfrastructure.org/projects/2894)

<img src="mapmate_logo.png" align="left"/>

# MapMate

MapMate solves the problem of (de)serialization and validation of Custom Primitives and general Serializable Objects 
in a clean and non-invasive way. MapMate enables you to 

* Keep validation of your domain in your domain - the factory methods
* Register your validation exceptions and report them aggregated upon deserialization
* Stay independent of the final format of your serialization (YAML/JSON/XML/... - you control)

## Rationale

Almost any modern application nowadays is dealing with mapping incoming requests to a subset of Domain Objects and 
outputting the resulting subset of Domain Objects to a specific format (be that json, yaml, xml, ...). 

As a result, we keep writing the "same" code over and over again. We 

* parse the Strings into Domain Objects, 
* we validate those upon initialization, 
* we process the requests and 
* we do the mapping again to output a certain Response.
 
While there are numerous frameworks that help you on some of the stages of this process, we believe there is a need for a simple,
 non-invasive library that would allow you to integrate this process into your application seamlessly and concentrate on 
 your Business logic.

For an introduction article checkout [envimate blog](https://blog.envimate.com/mapmate-intro) where we build a small
example application and explain the usage of Custom Primitives and Data Transfer Objects. 

## Highlighted features 
Some features MapMate offers:

 -  **No Magic** - MapMate is using your Objects the same way you would use them, which means:
    - MapMate will not 
        - read nor write private fields
        - write values into final fields 
        - invoke private methods
    - MapMate will not generate dynamic proxies
    - MapMate does not favour the use of annotations
        
 - **Support for validating your domain**
    - MapMate allows you to check for validation exceptions and aggregates them accordingly.
    - You will know precisely which field of which domain was faulty.
    - MapMate also offers ways of detecting redundant validation exceptions.
 - **Non-intrusive usage and configuration** 
    - MapMate instance can be configured in a single place and offers detection of Custom Primitives and Serialized 
        Objects without the use of annotations.
    - As mentioned above, MapMate creates and validates your Objects the way you would; hence you don't need to change
    anything in your domain when using MapMate. Since automatic configuration always requires conventions, MapMate allows
    you to follow _your_ conventions, instead of the ones it's coders came up with.
    -  That means your domain stays free of dependencies(direct or conventional) to the (de)serialization and validation 
    frameworks.  
 - **Highly customizable** 
    - MapMate is highly configurable, allowing configuration for 
        - whitelisting and blacklisting packages and/or classes(or even disable the whole classpath scanning and configure 
        objects one-by-one)
        - the detection mechanism of Custom Primitives and Serialized Objects
        - manual definition of Custom Primitives and Serialized Objects
        - customizing the Validation Errors

## Resources

Check out these resources, and let us know if you don't find the information you are looking for, 
we'll be happy to address that.

* [QuickStart](docs/QuickStart.md) - Minimal configuration and example usage of MapMate
* [User Guide](docs/UserGuide.md) - Detailed description of MapMate functionality, walkthrough features and configuration possibilities
* [Introduction Blogpost](https://github.com/envimate/mapmate) - Example application, rationale behind MapMate
* [Concepts](docs/Concepts.md) - Terminology we use explained. Check this document out to understand what we call [Custom Primitive](docs/Concepts.md#custom-primitives) and [Serialized Object](docs/Concepts.md#serialized-objects) and why we choose [String as the representation method](docs/Concepts.md#string-representation).

Also, take a look into these articles to get an idea of why we created MapMate in the first place:

* [Domain Driven Security](docs/articles/DomainDrivenSecurity.md)

## Contributing

[General Contribution Guidelines](https://github.com/envimate/.github/blob/master/CONTRIBUTING.md)

MapMate is quite young, and the best contribution is using it and giving us feedback.
 
Open issues, or drop an email to mapmate@envimate.com, let us know how you use it and which features you would like to see.
