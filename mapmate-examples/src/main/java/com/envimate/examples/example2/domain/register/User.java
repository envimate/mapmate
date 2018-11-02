package com.envimate.examples.example2.domain.register;

public final class User {

    public final AccountId accountId;
    public final UserName userName;
    public final UserEmail userEmail;

    private User(AccountId accountId, UserName userName, UserEmail userEmail) {
        this.accountId = accountId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public static final User user(
            final AccountId accountId,
            final UserName userName,
            final UserEmail userEmail
    ) {
        return new User(accountId, userName, userEmail);
    }

}
