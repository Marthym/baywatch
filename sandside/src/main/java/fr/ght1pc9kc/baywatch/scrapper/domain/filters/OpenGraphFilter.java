package fr.ght1pc9kc.baywatch.scrapper.domain.filters;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilterStep;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class OpenGraphFilter implements NewsFilter {
    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    private final HeadScraper headScrapper;

    @Override
    public NewsFilterStep getStep() {
        return NewsFilterStep.ENHANCE;
    }

    @Override
    public Mono<RawNews> filter(RawNews news) {
        try {
            if (!SUPPORTED_SCHEMES.contains(news.getLink().getScheme())) {
                return Mono.just(news);
            }
            return headScrapper.scrap(news.getLink())
                    .map(headMetas -> {
                        RawNews raw = news;

                        Links links = headMetas.links();
                        if (nonNull(links) && nonNull(links.canonical())) {
                            raw = raw.withId(Hasher.identify(links.canonical()))
                                    .withLink(links.canonical());
                        }

                        OpenGraph og = headMetas.og();
                        if (og.isEmpty()) {
                            log.debug("No OG meta found for {}", news.getLink());
                            return raw;
                        }
                        raw = Optional.ofNullable(og.title).map(raw::withTitle).orElse(raw);
                        raw = Optional.ofNullable(og.description).map(raw::withDescription).orElse(raw);
                        raw = Optional.ofNullable(og.image)
                                .filter(i -> SUPPORTED_SCHEMES.contains(i.getScheme()))
                                .map(raw::withImage).orElse(raw);
                        return raw;
                    }).switchIfEmpty(Mono.just(news));
        } catch (Exception e) {
            log.warn("Unable to scrap header from {}.", news.getLink());
            log.debug("STACKTRACE", e);
            return Mono.just(news);
        }
    }
}
