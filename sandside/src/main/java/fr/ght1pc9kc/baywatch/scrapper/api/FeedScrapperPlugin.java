package fr.ght1pc9kc.baywatch.scrapper.api;

import java.net.URI;

public interface FeedScrapperPlugin {
    String pluginForDomain();

    default URI uriModifier(URI original) {
        return original;
    }
}
