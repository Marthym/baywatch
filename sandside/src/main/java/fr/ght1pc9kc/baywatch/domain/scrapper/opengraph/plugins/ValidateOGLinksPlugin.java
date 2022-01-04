package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.plugins;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphPlugin;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.URITools;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

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
            return http.head().uri(openGraph.image).exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    log.debug("SUCCESS  {}", openGraph.image);
                    return Mono.just(openGraph);
                } else if (response.statusCode().is3xxRedirection()) {
                    String location = response.headers().header(HttpHeaderNames.LOCATION.toString()).get(0);
                    log.debug("REDIRECT {}", location);
                    log.debug("    FROM {}", openGraph.image);
                    return Mono.just(openGraph.withImage(URITools.removeQueryString(location)));
                } else {
                    log.debug("ERROR    {}", openGraph.image);
                    return Mono.just(openGraph.withImage(null));
                }
            });

        } else {
            return Mono.just(openGraph);
        }
    }
}
