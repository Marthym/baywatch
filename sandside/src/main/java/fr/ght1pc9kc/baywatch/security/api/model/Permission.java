package fr.ght1pc9kc.baywatch.security.api.model;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * A Permission represent a {@link Role} or an {@link Authorization}
 *
 * <ul>
 *     <li><b>Role</b>: Represent the master permission family</li>
 *     <li><b>Authorization</b>: Represent a permission for a specific {@code Entity} this apply a Role to an Entity</li>
 * </ul>
 */
public sealed interface Permission permits Role, Authorization {
    char ENTITY_SEPARATOR = ':';
    String PERMISSION_FORMAT = "%s" + ENTITY_SEPARATOR + "%s";

    Role role();

    Optional<String> entity();

    static Permission manager(String entity) {
        return new Authorization(Role.MANAGER, entity);
    }

    static Permission of(Role role, @Nullable String entity) {
        if (Objects.isNull(entity)) {
            return role;
        } else {
            return new Authorization(role, entity);
        }
    }

    static Permission from(String permissionRepresentation) {
        try {
            String[] split = permissionRepresentation.split(String.valueOf(ENTITY_SEPARATOR));
            Role role = Role.valueOf(split[0]);
            if (split.length == 1) {
                return role;
            } else if (split.length == 2 && !split[1].isBlank()) {
                return new Authorization(role, split[1]);
            } else {
                throw new IllegalArgumentException("Malformed permission representation");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse permission " + permissionRepresentation, e);
        }
    }

    String toString();
}
