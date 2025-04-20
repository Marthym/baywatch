package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawFeedPersistencePort;
import fr.ght1pc9kc.baywatch.admin.infra.mappers.RawFeedMapper;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.adapters.persistence.FeedRepository;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class RawFeedPersistenceAdapter implements RawFeedPersistencePort {
    private final FeedRepository feedRepository;
    private final RawFeedMapper mapper;

    @Override
    public Flux<Entity<RawFeed>> list(QueryContext qCtx) {
        return feedRepository.list(qCtx)
                .map(mapper::toRawFeed);
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        return feedRepository.count(qCtx);
    }

    @Override
    public Flux<Entity<RawFeed>> update(Collection<Entity<RawFeed>> toUpdate) {
        return Flux.fromIterable(toUpdate)
                .map(mapper::toWebFeed).collectList()
                .flatMapMany(feedRepository::update)
                .map(mapper::toRawFeed);
    }

    @Override
    public Flux<Entity<RawFeed>> persist(Collection<Entity<RawFeed>> toPersist) {
        return Flux.fromIterable(toPersist)
                .map(mapper::toWebFeed).collectList()
                .flatMapMany(feedRepository::persist)
                .map(mapper::toRawFeed);
    }

    @Override
    public Mono<Void> delete(QueryContext qCtx) {
        return feedRepository.delete(qCtx).then();
    }
}
