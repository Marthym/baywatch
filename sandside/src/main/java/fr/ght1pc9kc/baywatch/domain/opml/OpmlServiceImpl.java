package fr.ght1pc9kc.baywatch.domain.opml;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.security.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.security.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.infra.techwatch.persistence.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

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
    public Mono<Void> opmlImport(Flux<DataBuffer> data) {
        log.debug("Start importing...");
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMapMany(Exceptions.wrap().function(owner -> {
                    PipedOutputStream pos = new PipedOutputStream();
                    PipedInputStream pis = new PipedInputStream(pos);
                    Flux<Feed> feeds = readOpml(pis);
                    Mono<Feed> db = DataBufferUtils.write(data, pos)
                            .map(DataBufferUtils::release)
                            .doOnTerminate(Exceptions.wrap().runnable(() -> {
                                pos.flush();
                                pos.close();
                            }))
                            .then(Mono.empty());
                    return Flux.merge(db, feeds)
                            .buffer(100)
                            .flatMap(f -> feedRepository.persist(f, owner.id));
                })).then();
    }

    private Mono<Void> writeOpml(OutputStream out, Entity<User> owner) {
        OpmlWriter opmlWriter = writerFactory.apply(out);
        return feedRepository.list(QueryContext.empty().withUserId(owner.id))
                .doFirst(() -> opmlWriter.startOpmlDocument(owner.entity))
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

    private Flux<Feed> readOpml(InputStream is) {
        return Flux.create(sink ->
                readerFactory.create(sink::next, sink::complete, sink::error).read(is));
    }
}
