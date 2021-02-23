package fr.ght1pc9kc.baywatch.api.model;

import lombok.*;

@With
@Value
@Builder
@Getter(AccessLevel.NONE)
public class User {
    public static final User ANONYMOUS = new User(
            "0000000000000000000000000000000000000000000000000000000000000000",
            "anonymous", "Anonymous", "noreply@anomynous.org", null);

    public @NonNull String id;
    public @NonNull String login;
    public String name;
    public String mail;
    public String password;
}
