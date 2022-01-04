package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import lombok.experimental.UtilityClass;

import java.net.URI;

@UtilityClass
public class URITools {
    public URI removeQueryString(String uri) {
        int idx = uri.indexOf('?');
        return (idx < 0) ? URI.create(uri) : URI.create(uri.substring(0, idx));
    }

    public URI removeQueryString(URI uri) {
        return removeQueryString(uri.toString());
    }
}
