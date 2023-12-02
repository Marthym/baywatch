package fr.ght1pc9kc.baywatch.indexer.domain.model;

public interface IndexableVisitor<R> {
    R feed(FeedDocument idxFeed);

    R entry(EntryDocument idxEntry);
}
