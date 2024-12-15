package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.ScraperServicePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedProperties;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.NO_ONE;
import static fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException.AUTHENTICATION_NOT_FOUND;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.createdBy;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
                    if (pageRequest.filter().accept(propertiesVisitor).contains(ID)) {
                        return Tuples.of(QueryContext.from(pageRequest), u.id());
                    }
                    return Tuples.of(QueryContext.from(pageRequest).withUserId(u.id()), u.id());
                })
                .switchIfEmpty(Mono.just(Tuples.of(QueryContext.from(pageRequest), NO_ONE)))
                .flatMapMany(qc -> feedRepository.list(qc.getT1())
                        .map(re -> {
                            String createdByMeta = Arrays.stream(re.meta(createdBy).orElse(NO_ONE).split(","))
                                    .filter(u -> qc.getT2().equals(u))
                                    .findAny().orElse(NO_ONE);
                            return Entity.identify(re.self())
                                    .meta(createdBy, createdByMeta)
                                    .withId(re.id());
                        }))

                .buffer(Math.min(pageRequest.pagination().size(), 100))
                .flatMap(rawWebFeeds -> {
                    String createdBy = rawWebFeeds.getFirst().meta(FeedMeta.createdBy).orElse(null);
                    if (isNull(createdBy)) {
                        return Flux.fromIterable(rawWebFeeds);
                    }
                    List<String> feedsIds = rawWebFeeds.stream().map(Entity::id).toList();
                    return authFacade.getConnectedUser()
                            .map(Entity::id)
                            .switchIfEmpty(Mono.just(NO_ONE))
                            .flatMapMany(userId -> feedRepository.getFeedProperties(userId, feedsIds, null))
                            .map(eProps -> Map.entry(eProps.id(), eProps.self()))
                            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                            .flatMapMany(allProps -> overrideCustomizedFeedProperties(allProps, rawWebFeeds));
                });
    }

    private Flux<Entity<WebFeed>> overrideCustomizedFeedProperties(
            Map<String, Map<FeedProperties, String>> allPropertiesByFeed,
            List<Entity<WebFeed>> rawFeeds) {
        return Flux.fromIterable(rawFeeds)
                .map(webFeed -> {
                    Map<FeedProperties, String> feedProperties = allPropertiesByFeed.get(webFeed.id());
                    if (isNull(feedProperties)) {
                        return webFeed;
                    } else {
                        return webFeed.convert(self -> {
                            WebFeed.WebFeedBuilder selfBuilder = self.toBuilder();
                            feedProperties.forEach((feedProp, value) -> {
                                switch (feedProp) {
                                    case DESCRIPTION -> selfBuilder.description(value);
                                    case NAME -> selfBuilder.name(value);
                                    case TAG -> selfBuilder.tags(Set.of(value.split(",")));
                                    case null, default -> {/* Nothing to do if null */}
                                }
                            });
                            return selfBuilder.build();
                        });
                    }
                });
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        if (pageRequest.filter().accept(propertiesVisitor).contains(ID)) {
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
    public Flux<Entity<WebFeed>> add(Collection<Entity<WebFeed>> toAdd) {
        if (toAdd.stream().anyMatch(f -> (f == null
                || f.self().location().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(f.self().location().getScheme().toLowerCase())))) {
            return Flux.error(() -> new IllegalArgumentException("Illegal URL for Feed !"));
        }
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(u -> completeFeedData(toAdd))
                .flatMapMany(feedRepository::persist);
    }

    @Override
    public Flux<Entity<WebFeed>> subscribe(Collection<Entity<WebFeed>> feeds) {
        if (feeds.stream().anyMatch(f -> (f == null
                || f.self().location().getScheme() == null
                || !ALLOWED_PROTOCOL.contains(f.self().location().getScheme().toLowerCase())))) {
            return Flux.error(() -> new IllegalArgumentException("Illegal URL for Feed !"));
        }

        List<String> subscribedIds = feeds.stream().map(Entity::id).toList();
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(u -> feedRepository.persistUserRelation(u.id(), feeds)
                        .then().thenReturn(u))

                .flatMap(u -> feedRepository.list(QueryContext.all(Criteria.property(ID).in(subscribedIds)))
                        .collectMap(Entity::id, Function.identity())
                        .map(originals -> feeds.stream()
                                .map(feedEntity -> feedEntity.convert(ignore -> optimizeWebFeedToUpdate(feedEntity, originals)))
                                .toList())
                        .flatMap(optimisedFeeds -> feedRepository.setFeedProperties(u.id(), optimisedFeeds)))

                .thenMany(list(PageRequest.all(Criteria.property(ID)
                        .in(subscribedIds))));
    }

    private WebFeed optimizeWebFeedToUpdate(Entity<WebFeed> feedEntity, Map<String, Entity<WebFeed>> originals) {

        Entity<WebFeed> rawFeed = originals.get(feedEntity.id());
        if (isNull(rawFeed)) {
            return null;
        }
        WebFeed.WebFeedBuilder feedBuilder = WebFeed.builder()
                .location(rawFeed.self().location());
        final WebFeed feed = feedEntity.self();
        boolean isSame = true;
        if (nonNull(feed.name()) && !feed.name().equals(rawFeed.self().name())) {
            feedBuilder.name(feed.name());
            isSame = false;
        }
        if (nonNull(feed.description()) && !feed.description().equals(rawFeed.self().description())) {
            feedBuilder.description(feed.description());
            isSame = false;
        }
        if (!feed.tags().equals(rawFeed.self().tags())) {
            feedBuilder.tags(feed.tags());
            isSame = false;
        }
        if (isSame) {
            return null;
        } else {
            return feedBuilder.build();
        }
    }

    @Override
    public Flux<Entity<WebFeed>> addAndSubscribe(Collection<Entity<WebFeed>> feeds) {
        return add(feeds).thenMany(subscribe(feeds));
    }

    private Mono<? extends Collection<Entity<WebFeed>>> completeFeedData(Collection<Entity<WebFeed>> feeds) {
        return Flux.fromIterable(feeds)
                .parallel(4)
                .flatMap(f -> scraperService.fetchFeedData(f.self().location()).map(a -> Tuples.of(f, a)))
                .sequential()
                .map(t -> {
                    Entity<WebFeed> oldf = t.getT1();
                    WebFeed newf = t.getT2();
                    return oldf.convert(e -> e.toBuilder()
                            .description(newf.description())
                            .build());
                }).collectList();
    }

    @Override
    public Mono<Integer> unsubscribe(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(user -> feedRepository.deleteFeedProperties(user.id(), toDelete).thenReturn(user))
                .flatMap(user -> feedRepository.deleteUserRelations(user.id(), toDelete))
                .thenReturn(toDelete.size());
    }
}
