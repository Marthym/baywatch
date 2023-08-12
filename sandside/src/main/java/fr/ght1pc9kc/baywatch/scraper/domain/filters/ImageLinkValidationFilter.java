package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.unbescape.html.HtmlEscape;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ImageLinkValidationFilter implements NewsFilter {
    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    private final WebClient http;

    @Override
    public Mono<RawNews> filter(RawNews news) {
        if (Objects.isNull(news.image())
                || !SUPPORTED_SCHEMES.contains(news.image().getScheme())) {
            return Mono.just(news.withImage(null));
        }
        URI tested = (news.image().getQuery() == null) ? news.image() :
                URI.create(OGScrapperUtils.removeQueryString(news.image().toString())
                        + "?" + HtmlEscape.unescapeHtml(news.image().getQuery()));
        return http.get().uri(tested).exchangeToMono(ClientResponse::toBodilessEntity)
                .map(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.trace("SUCCESS  {}", tested);
                        if (tested.equals(news.image()))
                            return news;
                        else
                            return news.withImage(tested);

                    } else if (response.getStatusCode().is3xxRedirection()) {
                        URI location = Optional.ofNullable(
                                        response.getHeaders().get(HttpHeaderNames.LOCATION.toString()))
                                .map(l -> l.get(0))
                                .map(URI::create)
                                .orElse(tested);
                        log.trace("REDIRECT {}", location);
                        log.trace("    FROM {}", tested);
                        return news.withImage(location);

                    } else {
                        log.debug("BAD     {}", tested);
                        return news.withImage(null);
                    }
                }).onErrorResume(e -> {
                    log.info("Error on validate link {}", tested);
                    log.debug("{}: {}", e.getClass(), e.getLocalizedMessage());
                    return Mono.just(news.withImage(null));
                });
    }
}
