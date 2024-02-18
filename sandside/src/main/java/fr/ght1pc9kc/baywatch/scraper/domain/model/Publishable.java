package fr.ght1pc9kc.baywatch.scraper.domain.model;

import java.time.Instant;

@FunctionalInterface
public interface Publishable<T> {
    T publication(Instant pubDate);
}
