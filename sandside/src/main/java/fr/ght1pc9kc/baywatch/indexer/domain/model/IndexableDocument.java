package fr.ght1pc9kc.baywatch.indexer.domain.model;

public sealed interface IndexableDocument permits EntryDocument, FeedDocument {
    <R> R accept(IndexableVisitor<R> visitor);
}
