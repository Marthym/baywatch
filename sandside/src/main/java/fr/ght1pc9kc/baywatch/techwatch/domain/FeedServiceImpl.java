package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.ScraperServicePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException.AUTHENTICATION_NOT_FOUND;

@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private static final Set<String> ALLOWED_PROTOCOL = Set.of("http", "https");

    private final FeedPersistencePort feedRepository;
    private final ScraperServicePort scraperService;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Feed> get(String id) {
        return feedRepository.get(QueryContext.id(id));
    }

    @Override
    public Flux<Feed> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<Feed> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(u -> QueryContext.from(pageRequest).withUserId(u.id))
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(QueryContext.from(pageRequest)))
                .flatMapMany(feedRepository::list);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id))
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(QueryContext.all(pageRequest.filter())))
                .flatMap(feedRepository::count);
    }

    @Override
    public Flux<RawFeed> raw(PageRequest pageRequest) {
        return feedRepository.list(QueryContext.from(pageRequest))
                .map(Feed::getRaw);
    }

    @Override
    public Mono<Feed> update(Feed toPersist) {
        if (toPersist == null
                || toPersist.getUrl().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(toPersist.getUrl().getScheme().toLowerCase())) {
            return Mono.error(() -> new IllegalArgumentException("Illegal URL for Feed !"));
        }
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(u -> feedRepository.update(toPersist, u.id));
    }

    @Override
    public Mono<Void> persist(Collection<Feed> toPersist) {
        if (toPersist.stream().anyMatch(f -> (f == null
                || f.getUrl().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(f.getUrl().getScheme().toLowerCase())))) {
            return Mono.error(() -> new IllegalArgumentException("Illegal URL for Feed !"));
        }
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(u -> completeFeedData(toPersist).map(f -> Tuples.of(f, u)))
                .flatMap(t -> feedRepository.persist(t.getT1(), t.getT2().id));
    }

    private Mono<? extends Collection<Feed>> completeFeedData(Collection<Feed> feeds) {
        return Flux.fromIterable(feeds)
                .parallel(4)
                .flatMap(f -> scraperService.fetchFeedData(f.getUrl()).map(a -> Tuples.of(f, a)))
                .sequential()
                .map(t -> {
                    Feed oldf = t.getT1();
                    Feed newf = t.getT2();
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(oldf.getId())
                                    .description(newf.getDescription())
                                    .name(newf.getRaw().getName())
                                    .url(oldf.getUrl())
                                    .lastWatch(oldf.getLastWatch())
                                    .build())
                            .tags(oldf.getTags())
                            .name(oldf.getName())
                            .build();
                }).collectList();
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(u -> QueryContext.builder()
                        .filter(Criteria.property(EntitiesProperties.FEED_ID).in(toDelete)
                                .or(Criteria.property(EntitiesProperties.ID).in(toDelete)))
                        .userId(u.id)
                        .build())
                .flatMap(feedRepository::delete)
                .map(r -> r.unsubscribed);
    }
}
