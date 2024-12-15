package fr.ght1pc9kc.baywatch.opml.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.opml.api.OpmlService;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.FeedRepository;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class OpmlServiceImpl implements OpmlService {
    private final FeedRepository feedRepository;
    private final AuthenticationFacade authFacade;
    private final OpmlWriterFactory writerFactory;
    private final OpmlReaderFactory readerFactory;

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
    public Mono<Void> opmlImport(Supplier<InputStream> inputSupplier) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMapMany(Exceptions.wrap().function(owner -> {
                    InputStream is = inputSupplier.get();
                    return readOpml(is)
                            .buffer(100)
                            .flatMap(f -> feedRepository.persist(f).collectList())
                            .flatMap(f -> feedRepository.persistUserRelation(owner.id(), f))
                            .doOnTerminate(Exceptions.wrap().runnable(is::close));

                })).then();
    }

    private Mono<Void> writeOpml(OutputStream out, Entity<User> owner) {
        OpmlWriter opmlWriter = writerFactory.apply(out);
        return feedRepository.list(QueryContext.empty().withUserId(owner.id()))
                .doFirst(() -> opmlWriter.startOpmlDocument(owner.self()))
                .doOnEach(signal -> {
                    Entity<WebFeed> feed = signal.get();
                    if (feed != null) {
                        opmlWriter.writeOutline(feed.self());
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

    private Flux<Entity<WebFeed>> readOpml(InputStream is) {
        return Flux.create(sink ->
                readerFactory.create(sink::next, sink::complete, sink::error).read(is));
    }
}
