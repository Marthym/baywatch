package fr.ght1pc9kc.baywatch.scrapper.domain.filters;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditNewsFilter implements NewsFilter {
    private static final URI REDDIT = URI.create("https://www.reddit.com");
    private static final String LINK = "link";
    private static final Pattern LINK_EXTRACT_PATTERN =
            Pattern.compile("href=\"(?<" + LINK + ">[^\"]*)\">\\[link]", Pattern.MULTILINE);

    @Override
    public Mono<RawNews> filter(RawNews news) {
        if (!news.getLink().getHost().contains("reddit.com")) {
            return Mono.just(news);
        }

        URI realLink = news.getLink();
        String description = news.getDescription();
        String parsableContent = HtmlUtils.htmlUnescape(description);
        Matcher m = LINK_EXTRACT_PATTERN.matcher(parsableContent);
        if (m.find()) {
            URI link = URI.create(m.group(LINK));
            realLink = (link.isAbsolute()) ? link : REDDIT.resolve(link);
        }

        return Mono.just(
                news.withId(Hasher.identify(realLink))
                        .withLink(realLink)
        );
    }
}
