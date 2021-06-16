package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import java.net.URI;
import java.util.Map;

/**
 * Interface to implement in order to add a OG scrapper plugin.
 *
 * A OG Scrapper Plugin allow to include additional Cookie or Headers into scrapping http request before send it.
 */
public interface OpenGraphPlugin {
    default String name() {
        return this.getClass().getSimpleName() + " OpenGraph scrapper plugin";
    }

    /**
     * The let the scrapper to know if the plugin must be used for the current request
     *
     * @param location The URI of the request
     * @return true is the scrapper must use the plugin for the request
     */
    boolean isApplicable(URI location);

    /**
     * If the scrapper must use the plugin give the Cookie to include to the request
     *
     * @return A map of Cookie Name / Cookie Value
     */
    default Map<String, String> additionalCookies() {
        return Map.of();
    }

    /**
     * If the scrapper must use the plugin give the Headers to include to the request
     *
     * @return A map of Header Name / Header Value
     */
    default Map<String, String> additionalHeaders() {
        return Map.of();
    }
}
