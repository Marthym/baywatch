package fr.ght1pc9kc.baywatch.indexer.domain.model;

public record EntryDocument(
        String id,
        String title,
        String description
) implements IndexableDocument {
    @Override
    public <R> R accept(IndexableVisitor<R> visitor) {
        return visitor.entry(this);
    }
}
