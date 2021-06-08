package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import java.net.URI;
import java.util.Map;

public interface OpenGraphPlugin {
    default String name() {
        return this.getClass().getSimpleName() + " OpenGraph scrapper plugin";
    }

    boolean isApplicable(URI location);

    default Map<String, String> additionalCookies() {
        return Map.of();
    }

    default Map<String, String> additionalHeaders() {
        return Map.of();
    }
}
