package fr.ght1pc9kc.baywatch.security.api.model;

public enum Role {
    SYSTEM, ADMIN, MANAGER, USER;

    public static final String FORMAT = "%s:%s";

    public String authority() {
        return "ROLE_" + name();
    }

    public static String manager(String id) {
        if (id.length() > 28) {
            throw new IllegalArgumentException("Illegal entity ID !");
        }
        return String.format(FORMAT, Role.MANAGER, id);
    }
}
