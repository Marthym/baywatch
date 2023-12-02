package fr.ght1pc9kc.baywatch.security.api.model;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

record Authorization(Role role, String authorizedEntity) implements Permission, Comparable<Permission> {

    public Optional<String> entity() {
        return Optional.of(authorizedEntity());
    }

    @Override
    public String toString() {
        return String.format(PERMISSION_FORMAT, role().name(), authorizedEntity());
    }

    @Override
    public int compareTo(@NotNull Permission o) {
        return COMPARATOR.compare(this, o);
    }
}
