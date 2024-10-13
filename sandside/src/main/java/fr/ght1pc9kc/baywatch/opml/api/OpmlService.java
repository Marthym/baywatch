package fr.ght1pc9kc.baywatch.opml.api;

import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.function.Supplier;

public interface OpmlService {
    Mono<InputStream> opmlExport();

    Mono<Void> opmlImport(Supplier<InputStream> inputSupplier);
}
