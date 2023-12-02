package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditNewsFilter implements NewsFilter {
    private static final URI REDDIT = URI.create("https://www.reddit.com");
    private static final String LINK = "link";
    private static final Pattern LINK_EXTRACT_PATTERN =
            Pattern.compile("href=\"(?<" + LINK + ">[^\"]*)\">\\[link]", Pattern.MULTILINE);

    private final URI redditImage;

    public RedditNewsFilter(String redditImage) {
        this.redditImage = URI.create(redditImage);
    }

    @Override
    public Mono<RawNews> filter(RawNews news) {
        if (!news.link().getHost().contains("reddit.com")) {
            return Mono.just(news);
        }

        String description = news.description();
        String parsableContent = HtmlUtils.htmlUnescape(description);
        Matcher m = LINK_EXTRACT_PATTERN.matcher(parsableContent);
        if (m.find()) {
            URI link = URI.create(m.group(LINK));
            URI realLink = (link.isAbsolute()) ? link : REDDIT.resolve(link);
            if (!news.link().equals(realLink)) {
                return Mono.just(
                        news.withId(Hasher.identify(realLink))
                                .withLink(realLink));
            }
        }
        URI image = Optional.ofNullable(news.image()).orElse(redditImage);
        return Mono.just(news.withImage(image));
    }
}
