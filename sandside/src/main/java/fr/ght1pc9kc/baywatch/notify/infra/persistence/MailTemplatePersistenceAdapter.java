package fr.ght1pc9kc.baywatch.notify.infra.persistence;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.MailTemplatesRecord;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailTemplatePersistencePort;
import fr.ght1pc9kc.baywatch.notify.infra.mappers.MailTemplateMapper;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import static fr.ght1pc9kc.baywatch.dsl.tables.MailTemplates.MAIL_TEMPLATES;
import static fr.ght1pc9kc.baywatch.notify.infra.mappers.DbPropertiesMapper.MATE_PROPERTIES_MAPPING;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class MailTemplatePersistenceAdapter implements MailTemplatePersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(MATE_PROPERTIES_MAPPING::get);
    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final MailTemplateMapper mapper;

    @Override
    @SuppressWarnings("resource")
    public Flux<Entity<MailTemplate>> list(PageRequest pageRequest) {
        Condition conditions = pageRequest.filter().accept(JOOQ_CONDITION_VISITOR);
        var query = dsl.selectFrom(MAIL_TEMPLATES).where(conditions);
        return Flux.<MailTemplatesRecord>create(sink -> {
                    Cursor<MailTemplatesRecord> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<MailTemplatesRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                })
                .subscribeOn(databaseScheduler)
                .limitRate(Integer.MAX_VALUE - 1)
                .map(mapper::toEntity);

    }
}
