package fr.ght1pc9kc.baywatch.scraper.domain.model;

import java.net.URI;
import java.util.Optional;

public enum RssNamespaces {
    DCMI("http://purl.org/dc/elements/1.1/");

    private static final RssNamespaces[] VALUES = RssNamespaces.values();
    private final URI uri;

    RssNamespaces(String uri) {
        this.uri = URI.create(uri);
    }

    public static Optional<RssNamespaces> fromURI(String uri) {
        for (RssNamespaces ns : VALUES) {
            if (ns.uri.toString().equals(uri)) {
                return Optional.of(ns);
            }
        }
        return Optional.empty();
    }
}
