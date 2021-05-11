package fr.ght1pc9kc.baywatch.domain;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.FeedPersistencePort;
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
        return feedRepository.get(id);
    }

    @Override
    public Flux<Feed> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<Feed> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> pageRequest.and(Criteria.property("userId").eq(u.id)))
                .onErrorResume(UnauthenticatedUser.class, (e) -> Mono.just(pageRequest))
                .flatMapMany(feedRepository::list);
    }

    @Override
    public Flux<RawFeed> raw(PageRequest pageRequest) {
        return feedRepository.list(pageRequest)
                .map(Feed::getRaw);
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
                .flatMap(u -> feedRepository.delete(toDelete, u.id));
    }
}
