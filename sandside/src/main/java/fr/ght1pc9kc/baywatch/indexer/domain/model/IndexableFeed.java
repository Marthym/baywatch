package fr.ght1pc9kc.baywatch.indexer.domain.model;

import java.util.List;

public record IndexableFeed(
        String id,
        String title,
        String description,
        String link,
        String author,
        List<String> tags
) {
}
