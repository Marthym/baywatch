package fr.ght1pc9kc.baywatch.api.opml;

import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface OpmlService {
    Mono<InputStream> opmlExport();

    Mono<Void> opmlImport(InputStream is);
}
