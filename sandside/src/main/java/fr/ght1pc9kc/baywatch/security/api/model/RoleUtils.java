package fr.ght1pc9kc.baywatch.security.api.model;

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
     * @return {@code TRUE} if the user a the role for all entity or for the specified entity
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
}
