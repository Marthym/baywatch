package fr.ght1pc9kc.baywatch.indexer.domain.model;


import lombok.Builder;

@Builder(toBuilder = true)
public record Indexable(
        String id,
        String title,
        String description,
        String link,
        String contentTitles,
        String contentSummaries
) {
}
