package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsMembersRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamMemberPersistencePort;
import fr.ght1pc9kc.baywatch.teams.infra.mappers.TeamsMapper;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Result;
import org.jooq.Select;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.dsl.tables.TeamsMembers.TEAMS_MEMBERS;
import static fr.ght1pc9kc.baywatch.teams.infra.mappers.PropertiesMapper.TEAMS_MEMBERS_PROPERTIES_MAPPING;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class MembersPersistenceAdapter implements TeamMemberPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(TEAMS_MEMBERS_PROPERTIES_MAPPING::get);

    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final TeamsMapper teamsMapper;

    @Override
    @SuppressWarnings("resource")
    public Flux<Entity<TeamMember>> list(QueryContext qCtx) {
        Condition conditions = qCtx.filter().accept(JOOQ_CONDITION_VISITOR);
        if (qCtx.isScoped()) {
            conditions = conditions.and(TEAMS_MEMBERS.TEME_USER_ID.eq(qCtx.userId()));
        }
        Select<TeamsMembersRecord> select = JooqPagination.apply(
                qCtx.pagination(), TEAMS_MEMBERS_PROPERTIES_MAPPING,
                dsl.selectFrom(TEAMS_MEMBERS)
                        .where(conditions));

        return Flux.<TeamsMembersRecord>create(sink -> {
                    Cursor<TeamsMembersRecord> cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<TeamsMembersRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(teamsMapper::getMemberEntity);
    }

    @Override
    public Mono<Void> add(Collection<Entity<TeamMember>> requests) {
        Query[] queries = requests.stream()
                .map(teamsMapper::getTeamsMemberRecord)
                .map(r -> dsl.insertInto(TEAMS_MEMBERS).set(r)
                        .onConflict().doUpdate()
                        .set(TEAMS_MEMBERS.TEME_PENDING_FOR, TEAMS_MEMBERS.TEME_PENDING_FOR.bitOr(r.getTemePendingFor()))
                ).toArray(Query[]::new);
        return Mono.fromCallable(() -> dsl.batch(queries).execute())
                .subscribeOn(databaseScheduler).then();
    }

    @Override
    public Mono<Void> remove(String teamId, Collection<String> membersIds) {
        return Mono.fromCallable(() -> dsl.deleteFrom(TEAMS_MEMBERS)
                        .where(TEAMS_MEMBERS.TEME_TEAM_ID.eq(teamId)
                                .and(TEAMS_MEMBERS.TEME_USER_ID.in(membersIds)))
                        .execute()
                ).subscribeOn(databaseScheduler)
                .then();
    }

    @Override
    public Mono<Void> clear(Collection<String> teamsIds) {
        return Mono.fromCallable(() -> dsl.deleteFrom(TEAMS_MEMBERS)
                        .where(TEAMS_MEMBERS.TEME_TEAM_ID.in(teamsIds))
                        .execute()
                ).subscribeOn(databaseScheduler)
                .then();
    }
}
