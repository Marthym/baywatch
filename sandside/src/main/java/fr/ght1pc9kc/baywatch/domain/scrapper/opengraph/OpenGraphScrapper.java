package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Meta;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
public final class OpenGraphScrapper {
    private static final String HEAD_END_TAG = "</head>";
    private static final String META_PROPERTY = "property";
    private static final String META_NAME = "name";
    private static final String META_CONTENT = "content";
    private static final Pattern META_PATTERN = Pattern.compile("<meta(?:" +
            "[^>]*(?:" + META_NAME + "|" + META_PROPERTY + ")\\W*=\\W*(?<" + META_PROPERTY + ">[\\w:]*)[^>]" +
            "|[^>]*" + META_CONTENT + "\\s*=\\s*\\W(?<" + META_CONTENT + ">[^>\"']*)[^>]*" +
            "){2}/?>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private final WebClient http;

    private final OpenGraphMetaReader ogReader;

    private final List<OpenGraphPlugin> scrapperPlugins;

    private static Flux<Meta> extractMetaFromHead(String html) {
        if (html.isBlank()) {
            return Flux.empty();
        }
        int headStopIdx = html.indexOf(HEAD_END_TAG);
        String head = (headStopIdx > 0) ? html.substring(0, headStopIdx) : html;
        Matcher m = META_PATTERN.matcher(head);
        return Flux.<Tuple2<String, String>>create(sink -> {
            while (m.find()) {
                Optional<String> property = Optional.ofNullable(m.group(META_PROPERTY));
                Optional<String> content = Optional.ofNullable(m.group(META_CONTENT));
                sink.next(Tuples.of(property.orElse(""), content.orElse("")));
            }
            sink.complete();
        }).filter(t -> t.getT1().startsWith(Tags.OG_NAMESPACE)
        ).map(t -> new Meta(t.getT1(), t.getT2())
        ).doOnError(e -> {
            if (log.isDebugEnabled()) {
                log.debug("Error while parsing head:\n{}", head);
            }
        });
    }

    public Mono<OpenGraph> scrap(URI location) {
        WebClient.RequestHeadersSpec<?> uri = http.get().uri(location);
        for (OpenGraphPlugin scrapperPlugin : scrapperPlugins) {
            if (scrapperPlugin.isApplicable(location)) {
                scrapperPlugin.additionalCookies().forEach(uri::cookie);
                scrapperPlugin.additionalHeaders().forEach(uri::header);
            }
        }
        return uri
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToMono(response -> {
                    Charset responseCharset = response.headers().contentType()
                            .map(MimeType::getCharset)
                            .orElse(StandardCharsets.UTF_8);

                    CharsetDecoder charsetDecoder = responseCharset.newDecoder();
                    return response.bodyToFlux(DataBuffer.class)

                            .switchOnFirst((signal, fBuff) -> {
                                if (signal.hasValue()) {
                                    DataBuffer dataBuffer = signal.get();
                                    assert dataBuffer != null;
                                    try {
                                        //noinspection BlockingMethodInNonBlockingContext
                                        charsetDecoder.decode(dataBuffer.asByteBuffer());
                                    } catch (CharacterCodingException e) {
                                        DataBufferUtils.release(dataBuffer);
                                        return Flux.empty();
                                    }
                                }
                                return fBuff;
                            })

                            .scan(new StringBuilder(), (sb, buff) -> {
                                StringBuilder bldr = sb.append(buff.toString(responseCharset));
                                DataBufferUtils.release(buff);
                                return bldr;
                            })
                            .takeUntil(sb -> sb.indexOf(HEAD_END_TAG) >= 0)
                            .last();
                })
                .map(StringBuilder::toString)
                .doFirst(() -> log.trace("Receiving data from {}...", location))

                .flatMapMany(OpenGraphScrapper::extractMetaFromHead)
                .collectList()
                .filter(not(List::isEmpty))
                .map(metas -> ogReader.read(metas, location))

                .onErrorResume(e -> {
                    log.warn("{}", e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                    return Mono.empty();
                });

    }

}
