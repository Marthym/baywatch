package fr.ght1pc9kc.baywatch.admin.infra.model;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import lombok.Builder;

public record Statistics(
        Counter news,
        Counter feeds,
        Counter users
) {
    @Builder
    public Statistics {
        // Only for holding @Builder
    }
}
