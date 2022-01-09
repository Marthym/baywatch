package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.plugins;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphPlugin;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OGScrapperUtils;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
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

@Slf4j
@RequiredArgsConstructor
public class ValidateOGLinksPlugin implements OpenGraphPlugin {

    private final WebClient http;

    @Override
    public boolean isApplicable(URI location) {
        return true;
    }

    @Override
    public Mono<OpenGraph> postTreatment(OpenGraph openGraph) {
        if (Objects.nonNull(openGraph.image)) {
            URI tested = (openGraph.image.getQuery() == null) ? openGraph.image :
                    URI.create(OGScrapperUtils.removeQueryString(openGraph.image.toString())
                            + "?" + HtmlEscape.unescapeHtml(openGraph.image.getQuery()));
            return http.get().uri(tested).exchangeToMono(ClientResponse::toBodilessEntity)
                    .map(response -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            log.trace("SUCCESS  {}", tested);
                            if (tested.equals(openGraph.image))
                                return openGraph;
                            else
                                return openGraph.withImage(tested);

                        } else if (response.getStatusCode().is3xxRedirection()) {
                            URI location = Optional.ofNullable(
                                            response.getHeaders().get(HttpHeaderNames.LOCATION.toString()))
                                    .map(l -> l.get(0))
                                    .map(URI::create)
                                    .orElse(tested);
                            log.trace("REDIRECT {}", location);
                            log.trace("    FROM {}", tested);
                            return openGraph.withImage(location);

                        } else {
                            log.debug("BAD     {}", tested);
                            return openGraph.withImage(null);
                        }
                    });
        } else {
            return Mono.just(openGraph);
        }
    }
}
