package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamPersistencePort;
import fr.ght1pc9kc.baywatch.teams.infra.mappers.TeamsMapper;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.Select;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.dsl.tables.Teams.TEAMS;
import static fr.ght1pc9kc.baywatch.teams.infra.mappers.PropertiesMapper.TEAMS_PROPERTIES_MAPPING;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class TeamPersistenceAdapter implements TeamPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(TEAMS_PROPERTIES_MAPPING::get);

    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final TeamsMapper teamsMapper;

    @Override
    @SuppressWarnings("resource")
    public Flux<Entity<Team>> list(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);
        Select<TeamsRecord> select = JooqPagination.apply(
                qCtx.pagination, TEAMS_PROPERTIES_MAPPING,
                dsl.selectFrom(TEAMS)
                        .where(conditions));

        return Flux.<TeamsRecord>create(sink -> {
                    Cursor<TeamsRecord> cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<TeamsRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(teamsMapper::getTeamEntity);
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);
        return Mono.fromCallable(() -> dsl.fetchCount(dsl.selectFrom(TEAMS).where(conditions)))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Void> persist(Entity<Team> toPersist) {
        TeamsRecord teamsRecord = teamsMapper.getTeamRecord(toPersist);
        TeamsRecord update = teamsRecord.copy();
        update.reset(TEAMS.TEAM_CREATED_AT);
        update.reset(TEAMS.TEAM_CREATED_BY);

        return Mono.fromCallable(() ->
                        dsl.insertInto(TEAMS).set(teamsRecord)
                                .onConflict(TEAMS.TEAM_ID).doUpdate().set(update)
                                .execute()
                ).subscribeOn(databaseScheduler)
                .then();
    }

    @Override
    public Mono<Void> delete(Collection<String> ids) {
        return Mono.fromCallable(() ->
                        dsl.deleteFrom(TEAMS).where(TEAMS.TEAM_ID.in(ids)).execute()
                ).subscribeOn(databaseScheduler)
                .then();
    }
}
