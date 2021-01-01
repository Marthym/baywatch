package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.infra.request.filter.JooqConditionVisitor;
import fr.ght1pc9kc.baywatch.infra.request.filter.PredicateSearchVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UserRepository implements UserPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.USER_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<User> USER_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ConversionService conversionService;

    @Override
    public Mono<User> get(String id) {
        return list(Criteria.property("id").eq(id)).next();
    }

    @Override
    public Flux<User> list(Criteria criteria) {
        Condition conditions = criteria.visit(JOOQ_CONDITION_VISITOR);
        return Flux.<UsersRecord>create(sink -> {
            Cursor<UsersRecord> cursor = dsl.selectFrom(USERS).where(conditions).fetchLazy();
            sink.onRequest(n -> {
                int count = Long.valueOf(n).intValue();
                Result<UsersRecord> rs = cursor.fetchNext(count);
                rs.forEach(sink::next);
                if (rs.size() < count) {
                    sink.complete();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(r -> conversionService.convert(r, User.class))
                .filter(criteria.visit(USER_PREDICATE_VISITOR));
    }

    @Override
    public Flux<User> list() {
        return list(Criteria.none());
    }

    @Override
    public Flux<User> persist(Collection<User> toPersist) {
        List<UsersRecord> records = toPersist.stream()
                .map(n -> conversionService.convert(n, UsersRecord.class))
                .collect(Collectors.toList());

        return Mono.fromCallable(() ->
                dsl.loadInto(USERS)
                        .batchAll()
                        .onDuplicateKeyIgnore()
                        .onErrorIgnore()
                        .loadRecords(records)
                        .fieldsCorresponding()
                        .execute())
                .subscribeOn(databaseScheduler)
                .map(loader -> {
                    log.info("Load {} News with {} error(s) and {} ignored",
                            loader.processed(), loader.errors().size(), loader.ignored());
                    return loader;
                }).flatMapMany(loader -> Flux.fromIterable(toPersist));
    }

    @Override
    public Mono<Integer> delete(Collection<String> ids) {
        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    DSLContext txDsl = tx.dsl();
                    txDsl.deleteFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_USER_ID.in(ids)).execute();
                    txDsl.deleteFrom(NEWS_USER_STATE).where(NEWS_USER_STATE.NURS_USER_ID.in(ids)).execute();
                    return txDsl.deleteFrom(USERS).where(USERS.USER_ID.in(ids)).execute();
                }));
    }
}
