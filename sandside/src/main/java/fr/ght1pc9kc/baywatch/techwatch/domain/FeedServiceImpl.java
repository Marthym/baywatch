package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.DefaultMeta;
import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.ScraperServicePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.filter.CriteriaVisitor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.NO_ONE;
import static fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException.AUTHENTICATION_NOT_FOUND;

@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private static final Set<String> ALLOWED_PROTOCOL = Set.of("http", "https");

    private final FeedPersistencePort feedRepository;
    //FIXME: On ne veut pas cette dépendence. C'est pas dans ce sens qu'il faut le faire
    private final ScraperServicePort scraperService;
    private final AuthenticationFacade authFacade;
    private final CriteriaVisitor<List<String>> propertiesVisitor;

    @Override
    public Mono<Entity<WebFeed>> get(String id) {
        return feedRepository.get(QueryContext.id(id));
    }

    @Override
    public Flux<Entity<WebFeed>> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<Entity<WebFeed>> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .map(u -> {
                    if (pageRequest.filter().accept(propertiesVisitor).contains(EntitiesProperties.ID)) {
                        return Tuples.of(QueryContext.from(pageRequest), u.id());
                    }
                    return Tuples.of(QueryContext.from(pageRequest).withUserId(u.id()), u.id());
                })
                .switchIfEmpty(Mono.just(Tuples.of(QueryContext.from(pageRequest), NO_ONE)))
                .flatMapMany(qc -> feedRepository.list(qc.getT1())
                        .map(re -> {
                            String createdBy = Arrays.stream(re.meta(DefaultMeta.createdBy).orElse(NO_ONE).split(","))
                                    .filter(u -> qc.getT2().equals(u))
                                    .findAny().orElse(NO_ONE);
                            return Entity.identify(re.self())
                                    .meta(DefaultMeta.createdBy, createdBy)
                                    .withId(re.id());
                        }));
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        if (pageRequest.filter().accept(propertiesVisitor).contains(EntitiesProperties.ID)) {
            return feedRepository.count(QueryContext.from(pageRequest));
        } else {
            return authFacade.getConnectedUser()
                    .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                    .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id()))
                    .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(QueryContext.all(pageRequest.filter())))
                    .flatMap(feedRepository::count);
        }
    }

    @Override
    public Mono<Entity<WebFeed>> update(WebFeed toPersist) {
        if (toPersist == null
                || toPersist.location().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(toPersist.location().getScheme().toLowerCase())) {
            return Mono.error(() -> new IllegalArgumentException("Illegal URL for Feed !"));
        }
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(u -> feedRepository.update(toPersist.reference(), u.id(), toPersist));
    }

    @Override
    public Flux<Entity<WebFeed>> add(Collection<WebFeed> toAdd) {
        if (toAdd.stream().anyMatch(f -> (f == null
                || f.location().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(f.location().getScheme().toLowerCase())))) {
            return Flux.error(() -> new IllegalArgumentException("Illegal URL for Feed !"));
        }
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(u -> completeFeedData(toAdd))
                .flatMapMany(feedRepository::persist);
    }

    @Override
    public Flux<Entity<WebFeed>> subscribe(Collection<WebFeed> feeds) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(u -> Tuples.of(feeds, u.id()))
                .flatMapMany(t -> feedRepository.persistUserRelation(t.getT1(), t.getT2()));
    }

    @Override
    public Flux<Entity<WebFeed>> addAndSubscribe(Collection<WebFeed> feeds) {
        return add(feeds).thenMany(subscribe(feeds));
    }

    private Mono<? extends Collection<WebFeed>> completeFeedData(Collection<WebFeed> feeds) {
        return Flux.fromIterable(feeds)
                .parallel(4)
                .flatMap(f -> scraperService.fetchFeedData(f.location()).map(a -> Tuples.of(f, a)))
                .sequential()
                .map(t -> {
                    WebFeed oldf = t.getT1();
                    WebFeed newf = t.getT2();
                    return WebFeed.builder()
                            .reference(oldf.reference())
                            .description(newf.description())
                            .name(oldf.name())
                            .location(oldf.location())
                            .tags(oldf.tags())
                            .updated(newf.updated())
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
                        .userId(u.id())
                        .build())
                .flatMap(feedRepository::delete)
                .map(FeedDeletedResult::unsubscribed);
    }
}
