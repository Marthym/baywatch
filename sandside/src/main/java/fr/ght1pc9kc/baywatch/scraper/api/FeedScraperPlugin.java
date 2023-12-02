package fr.ght1pc9kc.baywatch.scraper.api;

import java.net.URI;

public interface FeedScraperPlugin {
    String pluginForDomain();

    default URI uriModifier(URI original) {
        return original;
    }
}
