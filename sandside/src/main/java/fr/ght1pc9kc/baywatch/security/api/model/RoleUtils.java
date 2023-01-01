package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return hasRole(user, expectedRole, null);
    }

    /**
     * Check if {@link User} has an expected {@link Role} or higher for a specified entity ID.
     *
     * @param user         The user to test
     * @param expectedRole The minimal expected role without the entity ID
     * @param entity       The entity id
     * @return {@code TRUE} if the user has the role for all entity or for the specified entity
     */
    public static boolean hasRole(User user, @NotNull Role expectedRole, @Nullable String entity) {
        Objects.requireNonNull(expectedRole);
        if (Objects.isNull(user)) {
            return false;
        }
        boolean hasRole = false;
        Set<String> userRoles = Objects.isNull(entity)
                ? user.roles.stream().map(r -> r.split(":")[0]).collect(Collectors.toUnmodifiableSet())
                : user.roles;

        for (Role role : Role.values()) {
            if (userRoles.contains(role.name()) ||
                    (Objects.nonNull(entity) && userRoles.contains(String.format(Role.FORMAT, role.name(), entity)))) {
                hasRole = true;
            }
            if (role == expectedRole) {
                return hasRole;
            }
        }
        return false;
    }

    /**
     * Return the {@link Role} prefixed by {@code "ROLE"} for Spring authority
     * or the Role Entity authority if the role string contains an entity ID
     *
     * @param role The role or the entity authority
     * @return The Spring authority string
     */
    public String toSpringAuthority(String role) {
        try {
            String[] roleEntity = role.split(":");
            Role verifiedRole = Role.valueOf(roleEntity[0]);
            if (roleEntity.length == 2) {
                return role;
            } else {
                return SPRING_ROLE_PREFIX + verifiedRole.name();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(role + " is not a valid Role", e);
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
            String[] roleEntity = authority.split(":");
            String role = (roleEntity[0].startsWith(SPRING_ROLE_PREFIX))
                    ? roleEntity[0].substring(SPRING_ROLE_PREFIX.length())
                    : roleEntity[0];
            Role verifiedRole = Role.valueOf(role);

            if (roleEntity.length == 2) {
                return String.format(Role.FORMAT, verifiedRole.name(), roleEntity[1]);
            } else {
                return verifiedRole.name();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(authority + " is not a valid Authority", e);
        }
    }
}
