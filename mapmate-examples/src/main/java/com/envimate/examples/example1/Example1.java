package com.envimate.examples.example1;

import com.envimate.examples.example1.domain.auth.AccountId;
import com.envimate.examples.example1.domain.auth.Password;
import com.envimate.examples.example1.domain.auth.UserAuth;
import com.envimate.examples.example1.domain.auth.UserName;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.serialization.Serializer;
import com.google.gson.Gson;

import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.mapmate.filters.ClassFilters.*;
import static com.envimate.mapmate.serialization.Serializer.aSerializer;

public final class Example1 {

    public static final void simpleSerialization() {
        final Serializer serializer = aSerializer()
                .withMarshaller(new Gson()::toJson)
                .thatScansThePackage("com.envimate.examples.example1.domain")
                .forCustomPrimitives()
                .filteredBy(
                        allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue"))
                .thatAre().serializedUsingTheMethodNamed("getValue")
                .thatScansThePackage("com.envimate.examples.example1.domain")
                .forDataTransferObjects()
                .filteredBy(
                        allBut(
                                allClassesThatHaveAPublicStringMethodWithZeroArgumentsNamed("getValue")))
                .thatAre().serializedByItsPublicFields()
                .build();

        final UserAuth userAuth = UserAuth.userAuth(
                AccountId.fromString("098431"),
                UserName.fromString("john_doe90"),
                Password.fromString("mysecret123")
        );

        final String json = serializer.serialize(userAuth);
        System.out.println(json);

//        Output:
//        {
//            "userName": "john_doe90",
//            "accountId": "098431",
//            "password": "mysecret123"
//        }
    }

    public static final void simpleDeserialization() {
        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(new Gson()::fromJson)
                .thatScansThePackage("com.envimate.examples.example1.domain")
                .forCustomPrimitives()
                .filteredBy(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument())
                .thatAre().deserializedUsingTheStaticMethodWithSingleStringArgument()
                .thatScansThePackage("com.envimate.examples.example1.domain")
                .forDataTransferObjects()
                .filteredBy(allBut(allClassesThatHaveAStaticFactoryMethodWithASingleStringArgument()))
                .thatAre().deserializedUsingTheSingleFactoryMethod()
                .build();

        final String json = "" +
                "{" +
                "        \"accountId\": \"098431\"," +
                "        \"userName\": \"john_doe90\"," +
                "        \"password\": \"mysecret123\"" +
                "}";

        final UserAuth userAuth = deserializer.deserialize(json, UserAuth.class);
        System.out.println(userAuth);
    }

}
