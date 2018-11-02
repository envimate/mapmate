package com.envimate.examples.example1.domain.auth;

public final class UserAuth {

    public final AccountId accountId;
    public final UserName userName;
    public final Password password;

    private UserAuth(AccountId accountId, UserName userName, Password password) {
        this.accountId = accountId;
        this.userName = userName;
        this.password = password;
    }

    public static final UserAuth userAuth(
            final AccountId accountId,
            final UserName userName,
            final Password password
    ) {
        return new UserAuth(accountId, userName, password);
    }

}
