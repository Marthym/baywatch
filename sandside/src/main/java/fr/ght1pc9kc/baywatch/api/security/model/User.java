package fr.ght1pc9kc.baywatch.api.security.model;

import lombok.*;

import java.time.Instant;

@With
@Value
@Builder
@Getter(AccessLevel.NONE)
public class User {
    public static final User ANONYMOUS = new User("anonymous", "Anonymous",
            "noreply@anomynous.org", null, Role.ANONYMOUS, Instant.EPOCH, "127.0.0.1");

    public @NonNull String login;
    public String name;
    public String mail;
    public String password;
    public @NonNull Role role;
    public Instant loginAt;
    public String loginIP;
}
