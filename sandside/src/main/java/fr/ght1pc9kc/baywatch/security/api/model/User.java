package fr.ght1pc9kc.baywatch.security.api.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Value
@Builder(toBuilder = true)
@Getter(AccessLevel.NONE)
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
        Set<Permission> perms = new HashSet<>();
        for (String role : roles) {
            try {
                perms.add(Permission.from(role));
            } catch (Exception e) {
                log.debug(role + " ignored, not de role !");
            }
        }
        List<Permission> verifiedRoles = new ArrayList<>(perms);
        verifiedRoles.sort(Permission.COMPARATOR);
        return this.toBuilder()
                .clearRoles()
                .roles(List.copyOf(verifiedRoles))
                .build();
    }
}
