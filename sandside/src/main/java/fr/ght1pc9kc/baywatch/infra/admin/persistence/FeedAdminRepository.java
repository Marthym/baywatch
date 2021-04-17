package fr.ght1pc9kc.baywatch.infra.admin.persistence;

import fr.ght1pc9kc.baywatch.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.admin.ports.FeedAdministrationPort;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.infra.request.filter.JooqConditionVisitor;
import fr.ght1pc9kc.baywatch.infra.request.filter.PredicateSearchVisitor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

@Slf4j
@Repository
@AllArgsConstructor
public class FeedAdminRepository implements FeedAdministrationPort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.FEEDS_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<RawFeed> FEEDS_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<RawFeed> get(String id) {
        return list(PageRequest.one(Criteria.property(EntitiesProperties.ID).eq(id))).next();
    }

    @Override
    public Flux<RawFeed> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<RawFeed> list(PageRequest pageRequest) {
        Condition conditions = pageRequest.filter.visit(JOOQ_CONDITION_VISITOR);
        return Flux.<Record>create(sink -> {
            Cursor<Record> cursor = dsl.select().from(FEEDS)
                    .leftJoin(FEEDS_USERS).on(FEEDS.FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID))
                    .where(conditions).fetchLazy();
            sink.onRequest(n -> {
                Result<Record> rs = cursor.fetchNext(Long.valueOf(n).intValue());
                rs.forEach(sink::next);
                if (rs.size() < n) {
                    sink.complete();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(baywatchMapper::recordToRawFeed)
                .filter(pageRequest.filter.visit(FEEDS_PREDICATE_VISITOR));
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return Mono.fromCallable(() -> dsl.transactionResult(ctx -> {
            DSLContext tx = ctx.dsl();
            tx.deleteFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_FEED_ID.in(toDelete)).execute();
            return tx.deleteFrom(FEEDS).where(FEEDS.FEED_ID.in(toDelete)).execute();
        }));
    }

}
