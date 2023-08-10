package fr.ght1pc9kc.baywatch.security.api.model;

import com.machinezoo.noexception.Exceptions;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Slf4j
@Value
@Builder(toBuilder = true)
@Getter(AccessLevel.NONE)
@SuppressWarnings("java:S6548")
public class User {
    public static final User ANONYMOUS = new User("anonymous", "Anonymous",
            "noreply@anomynous.org", null, List.of());

    public final @NonNull String login;
    public final String name;
    public final String mail;
    public final @With String password;
    public final @Singular
    @NonNull List<Permission> roles;

    public User withRoles(String... roles) {
        if (Objects.isNull(roles) || roles.length == 0) {
            return this.toBuilder().clearRoles().build();
        }
        List<Permission> verifiedRoles = Arrays.stream(roles)
                .map(Exceptions.silence().function(Permission::from))
                .filter(not(Optional::isEmpty))
                .map(Optional::get)
                .distinct()
                .sorted(Permission.COMPARATOR)
                .toList();
        return this.toBuilder()
                .clearRoles()
                .roles(List.copyOf(verifiedRoles))
                .build();
    }
}
