package fr.ght1pc9kc.baywatch.admin.infra.model;

import lombok.Builder;

public record Statistics(
        int news,
        int feeds,
        int users
) {
    @Builder
    public Statistics {
        // Only for holding @Builder
    }
}
