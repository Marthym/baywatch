package fr.ght1pc9kc.baywatch.indexer.domain.model;

public record IndexableFeedEntry(
        String feed,
        String title,
        String description
) {
}
