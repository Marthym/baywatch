package fr.ght1pc9kc.baywatch.indexer.infra.controllers;

import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class IndexerController {
    private final FeedIndexerService indexerService;

    @MutationMapping
    public Mono<Void> indexerBuildFeedIndex() {
        return indexerService.buildIndex()
                .doOnError(e -> {
                    log.atError()
                            .addArgument(e.getClass())
                            .addArgument(e.getLocalizedMessage())
                            .log("{}: {}");
                    log.atDebug().log("STACKTRACE", e);
                });
    }
}
