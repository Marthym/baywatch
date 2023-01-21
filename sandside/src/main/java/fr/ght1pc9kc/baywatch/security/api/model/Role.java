package fr.ght1pc9kc.baywatch.security.api.model;

public enum Role {
    SYSTEM, ADMIN, MANAGER, USER;

    public static final char ENTITY_SEPARATOR = ':';
    public static final String FORMAT = "%s" + ENTITY_SEPARATOR + "%s";

    public static String manager(String id) {
        if (id.length() > 28) {
            throw new IllegalArgumentException("Illegal entity ID !");
        }
        return String.format(FORMAT, Role.MANAGER, id);
    }
}
