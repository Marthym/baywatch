package fr.ght1pc9kc.baywatch.domain.opml;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;

import java.util.function.Consumer;

@FunctionalInterface
public interface OpmlReaderFactory {
    OpmlReader create(Consumer<Feed> onOutline, Runnable onComplete, Consumer<Throwable> onError);
}
