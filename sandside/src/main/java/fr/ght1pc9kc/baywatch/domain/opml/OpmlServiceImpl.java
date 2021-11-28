package fr.ght1pc9kc.baywatch.domain.opml;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Slf4j
@RequiredArgsConstructor
public class OpmlServiceImpl implements OpmlService {
    private final FeedService feedService;
    private final AuthenticationFacade authFacade;
    private final OpmlWriterFactory writerFactory;

    @Override
    public Mono<InputStream> opmlExport() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(Exceptions.wrap().function(owner -> {
                    PipedInputStream pis = new PipedInputStream();
                    PipedOutputStream pos = new PipedOutputStream(pis);
                    writeOpml(pos, owner)
                            .contextWrite(context -> authFacade.withAuthentication(owner))
                            .doOnTerminate(Exceptions.wrap().runnable(() -> {
                                pos.flush();
                                pos.close();
                            })).subscribe();
                    return pis;
                }));
    }

    @Override
    public Mono<Void> opmlImport(InputStream is) {
        return null;
    }

    private Mono<Void> writeOpml(OutputStream out, User owner) {
        OpmlWriter opmlWriter = writerFactory.apply(out);
        return feedService.list()
                .doFirst(() -> opmlWriter.startOpmlDocument(owner))
                .doOnEach(signal -> {
                    Feed feed = signal.get();
                    if (feed != null) {
                        opmlWriter.writeOutline(feed);
                    }
                })
                .doOnComplete(opmlWriter::endOmplDocument)
                .onErrorResume(e -> {
                    opmlWriter.endOmplDocument();
                    log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                    return Flux.empty();
                }).then();
    }
}
