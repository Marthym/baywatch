package fr.ght1pc9kc.baywatch.security.api.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.With;

import java.util.Arrays;
import java.util.Objects;
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
    public @Singular
    @NonNull Set<String> roles;

    public User withRoles(String... roles) {
        if (Objects.isNull(roles) || roles.length == 0) {
            return this.toBuilder().clearRoles().build();
        }
        Set<String> verifiedRoles = Arrays.stream(roles)
                .filter(Objects::nonNull)
                .filter(Permission::validate)
                .collect(Collectors.toUnmodifiableSet());
        return this.toBuilder()
                .clearRoles()
                .roles(verifiedRoles)
                .build();
    }
}
