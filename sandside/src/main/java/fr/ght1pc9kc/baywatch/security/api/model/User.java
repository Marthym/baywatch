package fr.ght1pc9kc.baywatch.security.api.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.util.Set;

@With
@Value
@Builder
@Getter(AccessLevel.NONE)
public class User {
    public static final User ANONYMOUS = new User("anonymous", "Anonymous",
            "noreply@anomynous.org", null, Set.of());

    public @NonNull String login;
    public String name;
    public String mail;
    public String password;
    public @NonNull Set<String> roles;
}
