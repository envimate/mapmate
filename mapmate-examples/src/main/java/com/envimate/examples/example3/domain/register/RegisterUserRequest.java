package com.envimate.examples.example3.domain.register;

public final class RegisterUserRequest {

    public final UserName userName;
    public final UserEmail userEmail;
    public final Password password;
    public final Address address1;
    public final Address address2;

    private RegisterUserRequest(final UserName userName, final UserEmail userEmail, final Password password, final Address address1, final Address address2) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
        this.address1 = address1;
        this.address2 = address2;
    }

    public static final RegisterUserRequest userAuth(
            final UserName userName,
            final UserEmail userEmail,
            final Password password,
            final Address address1,
            final Address address2
    ) {
        return new RegisterUserRequest(userName, userEmail, password, address1, address2);
    }

    @Override
    public String toString() {
        return "RegisterUserRequest{" +
                "userName=" + userName +
                ", userEmail=" + userEmail +
                ", password=" + password +
                ", address1=" + address1 +
                ", address2=" + address2 +
                '}';
    }
}
