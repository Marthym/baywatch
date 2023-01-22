package fr.ght1pc9kc.baywatch.security.api.model;

import java.util.Optional;

record Authorization(Role role, String authorizedEntity) implements Permission {
    public Optional<String> entity() {
        return Optional.of(authorizedEntity());
    }

    @Override
    public String toString() {
        return String.format(PERMISSION_FORMAT, role().name(), authorizedEntity());
    }
}
