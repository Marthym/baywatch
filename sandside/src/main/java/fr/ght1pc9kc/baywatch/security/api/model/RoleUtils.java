package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to help test {@link User} {@link Role}
 */
@UtilityClass
public final class RoleUtils {

    private static final String SPRING_ROLE_PREFIX = "ROLE_";
    private static final Entity<User> SYSTEM = Entity.identify(Role.SYSTEM.name(), User.builder()
            .name(Role.SYSTEM.name())
            .login(Role.SYSTEM.name().toLowerCase())
            .roles(Set.of(Role.SYSTEM.name())).build());

    public static Entity<User> getSystemUser() {
        return SYSTEM;
    }

    /**
     * Check if {@link User} has an expected {@link Role} or higher.
     *
     * @param user         The user to test
     * @param expectedRole The minimal expected role
     * @return {@code TRUE} if the user has the role
     */
    public static boolean hasRole(User user, @NotNull Role expectedRole) {
        return hasPermission(user, expectedRole);
    }

    /**
     * Check if {@link User} has an expected {@link Permission} or higher for a specified entity ID.
     *
     * @param user       The user to test
     * @param permission The permission check for the user
     * @return {@code TRUE} if the user entity or for the specified entity
     */
    public static boolean hasPermission(User user, @NotNull Permission permission) {
        if (Objects.isNull(user) || user.roles.isEmpty()) {
            return false;
        }

        return user.roles.stream()
                .map(Permission::from)
                .anyMatch(perm -> {
                    if (perm.equals(permission) || perm.role().ordinal() < permission.role().ordinal()) {
                        return true;
                    }
                    if (perm.role().ordinal() > permission.role().ordinal()) {
                        return false;
                    }
                    if (permission.entity().isEmpty()) {
                        return true;
                    }
                    if (perm.entity().isPresent()) {
                        return perm.entity().equals(permission.entity());
                    }
                    return false;
                });
    }

    /**
     * returns the set of {@link Entity#id} for which the {@link User} has the given {@link Role}
     *
     * @param user The user to inspect
     * @param role The checked role
     * @return The list of entities
     */
    public static Set<String> getEntitiesFor(User user, Role role) {
        if (Objects.isNull(user) || Objects.isNull(role)) {
            return Set.of();
        }

        return user.roles.stream()
                .map(Permission::from)
                .filter(perm -> role == perm.role() && perm.entity().isPresent())
                .map(perm -> perm.entity().orElse(""))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Return the {@link Role} prefixed by {@code "ROLE_"} for Spring authority
     * or the Permission authority if the role string contains an entity ID
     *
     * @param permission The permission to change into Spring Authority
     * @return The Spring authority string
     */
    public String toSpringAuthority(String permission) {
        try {
            Permission perm = Permission.from(permission);
            if (perm.entity().isPresent()) {
                return permission;
            } else {
                return SPRING_ROLE_PREFIX + perm.role().name();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(permission + " is not a valid Role", e);
        }
    }

    /**
     * Return the {@link Role} for application without Spring prefix if authority was role
     * return authorities if not
     *
     * @param authority The spring authority
     * @return The application role
     */
    public String fromSpringAuthority(String authority) {
        try {
            String[] withoutPrefix = authority.split(SPRING_ROLE_PREFIX);
            return Permission.from(withoutPrefix[withoutPrefix.length - 1]).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException(authority + " is not a valid Authority", e);
        }
    }
}
