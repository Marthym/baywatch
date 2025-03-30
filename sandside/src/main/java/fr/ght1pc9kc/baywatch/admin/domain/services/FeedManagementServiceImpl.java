package fr.ght1pc9kc.baywatch.admin.domain.services;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawFeedPersistencePort;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class FeedManagementServiceImpl implements FeedManagementService {
    private final RawFeedPersistencePort persistencePort;

    @Override
    public Mono<Entity<RawFeed>> get(String id) {
        return persistencePort.list(QueryContext.id(id)).next();
    }

    @Override
    public Flux<Entity<RawFeed>> find(PageRequest pageRequest) {
        return persistencePort.list(QueryContext.from(pageRequest));
    }

    @Override
    public Mono<Entity<RawFeed>> create(RawFeed rawFeed) {
        Entity<RawFeed> toBePersisted = Entity.identify(rawFeed)
                .withId(Hasher.identify(rawFeed.url()));
        return persistencePort.persist(List.of(toBePersisted)).next();
    }

    @Override
    public Mono<Entity<RawFeed>> update(String id, RawFeed rawFeed) {
        if (isNull(id) || !id.equals(Hasher.identify(rawFeed.url()))) {
            return Mono.error(() -> new IllegalArgumentException("Update url is not allowed !"));
        }
        Entity<RawFeed> toBePersisted = Entity.identify(rawFeed)
                .withId(id);
        return persistencePort.update(List.of(toBePersisted)).next();
    }

    @Override
    public Mono<Void> delete(Collection<String> ids) {
        return persistencePort.delete(ids);
    }
}
