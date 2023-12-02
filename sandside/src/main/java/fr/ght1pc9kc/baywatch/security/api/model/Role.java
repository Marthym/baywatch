package fr.ght1pc9kc.baywatch.security.api.model;

import java.util.Optional;

public enum Role implements Permission {
    SYSTEM, ADMIN, MANAGER, USER, ACTUATOR;

    @Override
    public Role role() {
        return this;
    }

    @Override
    public Optional<String> entity() {
        return Optional.empty();
    }


    @Override
    public String toString() {
        return this.name();
    }
}
