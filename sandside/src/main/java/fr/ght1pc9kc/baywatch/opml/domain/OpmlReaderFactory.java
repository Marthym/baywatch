package fr.ght1pc9kc.baywatch.opml.domain;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;

import java.util.function.Consumer;

@FunctionalInterface
public interface OpmlReaderFactory {
    OpmlReader create(Consumer<Feed> onOutline, Runnable onComplete, Consumer<Throwable> onError);
}
