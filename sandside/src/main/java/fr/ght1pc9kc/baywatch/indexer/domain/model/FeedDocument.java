package fr.ght1pc9kc.baywatch.indexer.domain.model;

public record FeedDocument(
        String id,
        String title,
        String description,
        String link,
        String tags
) implements IndexableDocument {
    @Override
    public <R> R accept(IndexableVisitor<R> visitor) {
        return visitor.feed(this);
    }
}
