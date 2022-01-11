package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
public final class OpenGraphScrapper {
    private static final String HEAD_END_TAG = "</head>";
    private static final Pattern CHARSET_EXTRACT = Pattern.compile("<meta.*?charset=[\"']?([^\"']+)");

    private final WebClient http;

    private final OpenGraphMetaReader ogReader;

    private final List<OpenGraphPlugin> scrapperPlugins;

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
                    AtomicReference<Charset> responseCharset = response.headers().contentType()
                            .map(MimeType::getCharset)
                            .map(AtomicReference::new)
                            .orElseGet(() -> new AtomicReference<>(StandardCharsets.UTF_8));

                    CharsetDecoder charsetDecoder = responseCharset.get().newDecoder();
                    return response.bodyToFlux(DataBuffer.class)
                            .doOnTerminate(response::releaseBody)

                            .switchOnFirst((signal, fBuff) -> {
                                if (signal.hasValue()) {
                                    DataBuffer dataBuffer = signal.get();
                                    assert dataBuffer != null;
                                    try {
                                        //noinspection BlockingMethodInNonBlockingContext
                                        charsetDecoder.decode(dataBuffer.asByteBuffer());
                                    } catch (CharacterCodingException e) {
                                        try {
                                            Matcher m = CHARSET_EXTRACT.matcher(dataBuffer.toString(responseCharset.get()));
                                            if (m.find()) {
                                                responseCharset.set(Charset.forName(m.group(1)));
                                                return fBuff;
                                            }
                                        } catch (Exception ex) {
                                            log.trace("Unable to parse charset encoding from {}", location);
                                            log.trace("STACKTRACE", e);
                                        }
                                        DataBufferUtils.release(dataBuffer);
                                        return Flux.empty();
                                    }
                                }
                                return fBuff;
                            })

                            .scan(new StringBuilder(), (sb, buff) -> {
                                StringBuilder bldr = sb.append(buff.toString(responseCharset.get()));
                                DataBufferUtils.release(buff);
                                return bldr;
                            })
                            .takeUntil(sb -> sb.indexOf(HEAD_END_TAG) >= 0)
                            .last();
                })
                .map(StringBuilder::toString)
                .doFirst(() -> log.trace("Receiving data from {}...", location))

                .flatMapMany(OGScrapperUtils::extractMetaHeaders)
                .collectList()
                .filter(not(List::isEmpty))
                .map(metas -> ogReader.read(metas, location))

                .flatMap(og -> {
                    Mono<OpenGraph> resultOg = Mono.just(og);
                    for (OpenGraphPlugin scrapperPlugin : scrapperPlugins) {
                        if (scrapperPlugin.isApplicable(location)) {
                            resultOg = resultOg.flatMap(scrapperPlugin::postTreatment);
                        }
                    }
                    return resultOg;
                })

                .onErrorResume(e -> {
                    log.warn("{}", e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                    return Mono.empty();
                });

    }
}
