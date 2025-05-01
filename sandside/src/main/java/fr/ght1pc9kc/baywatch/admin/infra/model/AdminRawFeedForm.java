package fr.ght1pc9kc.baywatch.admin.infra.model;

import java.net.URI;

public record AdminRawFeedForm(
        String name,
        String description,
        URI url,
        URI icon,
        String lastWatch,
        String lastETag
) {
}
