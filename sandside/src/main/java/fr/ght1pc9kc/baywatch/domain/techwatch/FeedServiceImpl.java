package fr.ght1pc9kc.baywatch.domain.techwatch;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.common.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedPersistencePort feedRepository;
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
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> QueryContext.from(pageRequest).withUserId(u.id))
                .onErrorResume(UnauthenticatedUser.class, (e) -> Mono.just(QueryContext.from(pageRequest)))
                .flatMapMany(feedRepository::list);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id))
                .onErrorResume(UnauthenticatedUser.class, (e) -> Mono.just(QueryContext.all(pageRequest.filter())))
                .flatMap(feedRepository::count);
    }

    @Override
    public Flux<RawFeed> raw(PageRequest pageRequest) {
        return feedRepository.list(QueryContext.from(pageRequest))
                .map(Feed::getRaw);
    }

    @Override
    public Mono<Feed> update(Feed toPersist) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMap(u -> feedRepository.update(toPersist, u.id));
    }

    @Override
    public Mono<Void> persist(Collection<Feed> toPersist) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMap(u -> feedRepository.persist(toPersist, u.id));
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> QueryContext.builder()
                        .filter(Criteria.property(EntitiesProperties.FEED_ID).in(toDelete)
                                .or(Criteria.property(EntitiesProperties.ID).in(toDelete)))
                        .userId(u.id)
                        .build())
                .flatMap(feedRepository::delete)
                .map(r -> r.unsubscribed);
    }
}
