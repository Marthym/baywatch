package fr.ght1pc9kc.baywatch.scraper.domain.plugins;

import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Component
public final class RedditParserPlugin implements FeedScraperPlugin {

    private static final URI REDDIT = URI.create("https://www.reddit.com");

    @Override
    public String pluginForDomain() {
        return REDDIT.getHost();
    }

    @Override
    public URI uriModifier(URI original) {
        if (original.getQuery() == null) {
            return URI.create(original + "?sort=new");
        }

        Set<String> queryParams = Pattern.compile("&")
                .splitAsStream(original.getQuery())
                .map(s -> decode(s.split("=", 2)[0]))
                .filter(not(String::isBlank))
                .collect(Collectors.toUnmodifiableSet());

        if (queryParams.isEmpty()) {
            return URI.create((original + "?sort=new").replaceAll("\\?+", "?"));
        } else if (!queryParams.contains("sort")) {
            return URI.create(original + "&sort=new");
        } else {
            return original;
        }
    }

    private static String decode(final String encoded) {
        if (encoded == null) {
            return null;
        } else {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        }
    }
}
