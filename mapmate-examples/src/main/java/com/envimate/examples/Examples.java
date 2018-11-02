package com.envimate.examples;

import static com.envimate.examples.example1.Example1.simpleDeserialization;
import static com.envimate.examples.example1.Example1.simpleSerialization;
import static com.envimate.examples.example2.Example2.simpleValidatedDeserialization;
import static com.envimate.examples.example3.Example3.complexNestedValidatedDeserialization;
import static com.envimate.examples.example3.Example3.simpleNestedValidatedDeserialization;
import static com.envimate.examples.example4.Example4.xmlDeserialization;
import static com.envimate.examples.example4.Example4.xmlSerialization;
import static com.envimate.examples.example5.Example5.yamlDeserialization;
import static com.envimate.examples.example5.Example5.yamlSerialization;
import static com.envimate.examples.example6.Example6.*;
import static com.envimate.examples.example7.Example7.*;
import static com.envimate.examples.example8.Example8.deserializeWithInjectedTransientInterfaces;
import static com.envimate.examples.example8.Example8.serializingWithTransientFields;

/**
 * MapMate Examples
 */
public final class Examples {
    public static void main(String[] args) {
        simpleSerialization();
        simpleDeserialization();
        simpleValidatedDeserialization();
        simpleNestedValidatedDeserialization();
        complexNestedValidatedDeserialization();
        xmlSerialization();
        xmlDeserialization();
        yamlSerialization();
        yamlDeserialization();
        packageScanningClassFiltersCustom();
        packageScanningClassFiltersSuffix();
        packageScanningExclusionAndAdditions();
        packageScanningCircularReference();
        packageScanningUnknownReferenceType();
        packageScanningSerializingMethods();
        serializingUsingInjectors();
        deserializingUsingInjectors();
        deserializingUsingInjectorsAdvanced();
        serializingWithTransientFields();
        deserializeWithInjectedTransientInterfaces();
    }
}
