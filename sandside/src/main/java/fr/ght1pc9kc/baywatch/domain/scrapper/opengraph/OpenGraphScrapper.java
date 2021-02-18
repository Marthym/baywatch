package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Meta;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AllArgsConstructor
public final class OpenGraphScrapper {
    private static final Pattern META_PATTERN = Pattern.compile("<meta ([^>]*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final String HEAD_END_TAG = "</head>";

    private final WebClient http = WebClient.create();

    private static Flux<Meta> extractMetaFromHead(String head) {
        Matcher m = META_PATTERN.matcher(head);
        return Flux.<String>create(sink -> {
            while (m.find()) {
                sink.next(m.group(1));
            }
            sink.complete();
        })
                .map(s -> {
                    int propIdx = s.indexOf("property");
                    if (propIdx < 0) {
                        propIdx = s.indexOf("name");
                    }
                    if (propIdx < 0) {
                        return Tuples.of("", "");
                    }
                    int beginIndex = s.indexOf('"', propIdx + 1);
                    String property = s.substring(beginIndex + 1, s.indexOf('"', beginIndex + 1));
                    int contentIdx = s.indexOf("content");
                    if (contentIdx < 0) {
                        return Tuples.of(property, "");
                    }
                    beginIndex = s.indexOf('"', contentIdx + 1);
                    String content = s.substring(beginIndex + 1, s.indexOf('"', beginIndex + 1));

                    return Tuples.of(property, content);
                })
                .filter(t -> t.getT1().startsWith(Tags.OG_NAMESPACE))
                .map(t -> new Meta(t.getT1(), t.getT2()));
    }

    public Mono<OpenGraph> scrap(URI location) {
        return http.get().uri(location)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(DataBuffer.class)

                .scan(new StringBuilder(), (sb, buff) -> {
                    StringBuilder bldr = sb.append(buff.toString(StandardCharsets.UTF_8));
                    DataBufferUtils.release(buff);
                    return bldr;
                })
                .takeUntil(sb -> sb.indexOf(HEAD_END_TAG) >= 0)
                .last()
                .map(StringBuilder::toString)

                .flatMapMany(OpenGraphScrapper::extractMetaFromHead)
                .collectList()
                .map(OpenGraph::fromMetas)

                .doFirst(() -> log.trace("Receiving data from {}...", location.getHost()))
                .onErrorResume(e -> {
                    log.error("{}", e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                    return Mono.empty();
                }).doOnTerminate(() -> log.debug("terminate"));

    }

}
