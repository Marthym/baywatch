package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.LinkCheckPort;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.unbescape.html.HtmlEscape;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ImageLinkValidationFilter implements NewsFilter {
    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    private final LinkCheckPort linkCheckPort;

    @Override
    public Mono<RawNews> filter(RawNews news) {
        if (Objects.isNull(news.image())
                || !SUPPORTED_SCHEMES.contains(news.image().getScheme())) {
            return Mono.just(news.withImage(null));
        }
        URI tested = (news.image().getQuery() == null) ? news.image() :
                URI.create(OGScrapperUtils.removeQueryString(news.image().toString())
                        + "?" + HtmlEscape.unescapeHtml(news.image().getQuery()));

        return linkCheckPort.check(tested)
                .map(news::withImage)
                .switchIfEmpty(Mono.just(news.withImage(null)))
                .onErrorResume(e -> {
                    log.info("Error on validate link {}", tested);
                    log.debug("{}: {}", e.getClass(), e.getLocalizedMessage());
                    return Mono.just(news.withImage(null));
                });
    }
}
