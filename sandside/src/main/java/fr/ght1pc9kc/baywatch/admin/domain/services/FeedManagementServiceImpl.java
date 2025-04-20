package fr.ght1pc9kc.baywatch.admin.domain.services;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawFeedPersistencePort;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawNewsPersistencePort;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class FeedManagementServiceImpl implements FeedManagementService {
    private final RawFeedPersistencePort feedsPersistencePort;
    private final RawNewsPersistencePort newsPersistencePort;

    @Override
    public Mono<Entity<RawFeed>> get(String id) {
        return feedsPersistencePort.list(QueryContext.id(id)).next();
    }

    @Override
    public Flux<Entity<RawFeed>> find(PageRequest pageRequest) {
        return feedsPersistencePort.list(QueryContext.from(pageRequest));
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return feedsPersistencePort.count(QueryContext.all(pageRequest.filter()));
    }

    @Override
    public Mono<Entity<RawFeed>> create(RawFeed rawFeed) {
        Entity<RawFeed> toBePersisted = Entity.identify(rawFeed)
                .withId(Hasher.identify(rawFeed.url()));
        return feedsPersistencePort.persist(List.of(toBePersisted)).next();
    }

    @Override
    public Mono<Entity<RawFeed>> update(String id, RawFeed rawFeed) {
        if (isNull(id) || !id.equals(Hasher.identify(rawFeed.url()))) {
            return Mono.error(() -> new IllegalArgumentException("Update url is not allowed !"));
        }
        Entity<RawFeed> toBePersisted = Entity.identify(rawFeed)
                .withId(id);
        return feedsPersistencePort.update(List.of(toBePersisted)).next();
    }

    @Override
    public Mono<Void> delete(Collection<String> ids) {
        return newsPersistencePort.delete(QueryContext.all(Criteria.property(FEED_ID).in(ids)))
                .then(feedsPersistencePort.delete(QueryContext.all(Criteria.property(ID).in(ids))));
    }
}
