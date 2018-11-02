package com.envimate.examples.example2.domain.register;

public final class RegisterUserRequest {

    public final UserName userName;
    public final UserEmail userEmail;
    public final Password password;

    private RegisterUserRequest(UserName userName, UserEmail userEmail, Password password) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
    }

    public static final RegisterUserRequest userAuth(
            final UserName userName,
            final UserEmail userEmail,
            final Password password
    ) {
        return new RegisterUserRequest(userName, userEmail, password);
    }

}
