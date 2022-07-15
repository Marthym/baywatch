package fr.ght1pc9kc.baywatch.indexer.domain.model;

public interface IndexableVisitor<R> {
    R feed(IndexableFeed idxFeed);

    R entry(IndexableFeedEntry idxEntry);
}
