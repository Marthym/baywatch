package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import lombok.experimental.UtilityClass;

import java.net.URI;

@UtilityClass
public class URITools {
    public String removeQueryString(String uri) {
        int idx = uri.indexOf('?');
        return (idx < 0) ? uri : uri.substring(0, idx);
    }

    public URI removeQueryString(URI uri) {
        if (uri.getQuery() == null) {
            return uri;
        } else {
            return URI.create(removeQueryString(uri.toString()));
        }
    }
}
