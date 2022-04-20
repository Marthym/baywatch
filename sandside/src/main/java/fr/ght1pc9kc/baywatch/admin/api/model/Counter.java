package fr.ght1pc9kc.baywatch.admin.api.model;

public record Counter(
        String name,
        String value,
        String description
) {
    public static final Counter NONE = new Counter("None", "N/A", "Empty counter");
}
