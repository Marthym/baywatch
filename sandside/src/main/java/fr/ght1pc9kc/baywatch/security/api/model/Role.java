package fr.ght1pc9kc.baywatch.security.api.model;

public enum Role {
    SYSTEM, ADMIN, MANAGER, USER, ANONYMOUS;

    public String authority() {
        return "ROLE_" + name();
    }
}
