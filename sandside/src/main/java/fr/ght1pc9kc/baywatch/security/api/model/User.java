package fr.ght1pc9kc.baywatch.security.api.model;

import com.machinezoo.noexception.Exceptions;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.With;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Builder(toBuilder = true)
public record User(
        @NonNull String login,
        String name,
        String mail,
        @With String password,
        @Singular
        @NonNull List<Permission> roles
) {
    public static final User ANONYMOUS = new User("anonymous", "Anonymous",
            "noreply@anomynous.org", null, List.of());

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
