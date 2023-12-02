package fr.ght1pc9kc.baywatch.opml.domain;

import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;

import java.util.function.Consumer;

@FunctionalInterface
public interface OpmlReaderFactory {
    OpmlReader create(Consumer<WebFeed> onOutline, Runnable onComplete, Consumer<Throwable> onError);
}
