package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.FeedsFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.LinkCheckPort;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.net.URI;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class FaviconFeedFilter implements FeedsFilter {
    private final HeadScraper headScraper;
    private final LinkCheckPort linkCheckPort;

    @Override
    public Mono<AtomFeed> filter(@NotNull AtomFeed feed) {
        if (isNull(feed.link())) {
            return Mono.just(feed);
        }
        URI favicon = URI.create(String.format("%s://%s/favicon.ico",
                feed.link().getScheme(), feed.link().getAuthority()));
        return headScraper.scrap(URI.create(
                        String.format("%s://%s/", feed.link().getScheme(), feed.link().getAuthority())))
                .filter(metas -> nonNull(metas.links().icon()))
                .map(metas -> metas.links().icon())
                .onErrorResume(ignore -> Mono.empty())
                .switchIfEmpty(linkCheckPort.check(favicon))
                .map(icon -> feed.toBuilder()
                        .icon(icon)
                        .build())
                .onErrorResume(t -> {
                    log.atInfo().addArgument(t.getClass()).addArgument(t.getLocalizedMessage())
                            .log("{}: {}");
                    log.atDebug().log("STACKTRACE", t);
                    return Mono.just(feed);
                });
    }
}
