package fr.ght1pc9kc.baywatch.api.security.model;

public enum Role {
    SYSTEM, ADMIN, MANAGER, USER, ANONYMOUS;

    public String authority() {
        return "ROLE_" + name();
    }
}
