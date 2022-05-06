package fr.ght1pc9kc.baywatch.opml.api;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface OpmlService {
    Mono<InputStream> opmlExport();

    Mono<Void> opmlImport(Flux<DataBuffer> data);
}
