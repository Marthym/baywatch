package fr.ght1pc9kc.baywatch.security.api.model;

import com.machinezoo.noexception.Exceptions;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.With;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder(toBuilder = true)
@Getter(AccessLevel.NONE)
public class User {
    public static final User ANONYMOUS = new User("anonymous", "Anonymous",
            "noreply@anomynous.org", null, Set.of());

    public @NonNull String login;
    public String name;
    public String mail;
    public @With String password;
    public @Singular @NonNull Set<String> roles;

    public User withRoles(String... roles) {
        Set<String> verifiedRoles = Arrays.stream(roles)
                .filter(Exceptions.silence().<String>predicate(role -> {
                    Role.valueOf(role.split(":")[0]);
                    return true;
                }).orElse(false))
                .collect(Collectors.toUnmodifiableSet());
        return this.toBuilder()
                .roles(verifiedRoles)
                .build();
    }
}
