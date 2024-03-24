package fr.ght1pc9kc.baywatch.opml.domain;

import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;

import java.util.function.Consumer;

@FunctionalInterface
public interface OpmlReaderFactory {
    OpmlReader create(Consumer<Entity<WebFeed>> onOutline, Runnable onComplete, Consumer<Throwable> onError);
}
