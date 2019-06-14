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
outputting resulting subset of Domain Objects to a certain format (be that json, yaml, xml, ...). 

As a result we keep writing the "same" code over and over again. We 

* parse the Strings into Domain Objects, 
* we validate those upon initialization, 
* we process the requests and 
* we do the mapping again to output a certain Response.
 
While there are numerous frameworks that help you on some of the stages of this process, we believe there is a need of a simple,
 non-invasive library that would allow you to integrate this process into your application seamlessly and concentrate on 
 your Business logic.

For an introduction article checkout [envimate blog](https://blog.envimate.com/mapmate-intro) where we build a small
example application and explain the usage of Custom Primitives and Data Transfer Objects. 

## Highlighted features 
Some features MapMate offers:

 -  **No Magic** - MapMate is using your Objects the same way you would use them, which means:
    - MapMate can not touch
        - private fields
        - final fields 
        - private methods
    - MapMate will not generate dynamic proxies
    - MapMate does not favor the use of annotations
        
 - **Support for validating your domain**
    - MapMate allows you to check for validation exceptions and aggregates them accordingly.
    - You will know exactly what field of what domain was faulty.
    - MapMate also offers ways of detecting redundant validation exceptions.
 - **Non-intrusive usage and configuration** 
    - MapMate instance can be configured in a single place and offers detection of Custom Primitives and Serialized 
        Objects without the use of annotations.
    - As mentioned above, MapMate creates and validates your Objects the way you would, hence you don't need to change
    anything in your domain, when using MapMate. Since automatic configuration always requires conventions, MapMate allows
    you to follow _your_ conventions, instead of the ones it's coders came up with.
    -  That means your domain stays free of dependencies(direct or conventional) to the (de)serialization and validation 
    frameworks.  
 - **Highly customizable** 
    - MapMate is highly configurable, allowing configuration for 
        - whitelisting and blacklisting packages and/or classes(or even disable the whole class path scanning and configure 
        objects one-by-one)
        - detection mechanism of Custom Primitives and Serialized Objects
        - manual definition of Custom Primitives and Serialized Objects
        - customizing the Validation Errors
    - An all of this is can be achieved through a powerful builder

## Resources

Checkout these resources, and let us know if you don't find the information you are looking for, 
we'll be happy to address that.

* [User Guide](docs/UserGuide.md)
* [Recipes](docs/Recipes.md)
* [Introduction Blogpost](https://github.com/envimate/mapmate)
* [Concepts](docs/Concepts.md) 

Also checkout related articles to get an idea of why we created MapMate in the first place:

* [Data Driven Security](docs/articles/DomainDrivenSecurity.md)

## Contributing

[General Contribution Guidelines](https://github.com/envimate/.github/blob/master/CONTRIBUTING.md)

MapMate is quite young, and the best contribution is using it and giving us feedback.
 
Open issues, or drop an email to mapmate@envimate.com, let us know how you use it and which features you would like to see.
