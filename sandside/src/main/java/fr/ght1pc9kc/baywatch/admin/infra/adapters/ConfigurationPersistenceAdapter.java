package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.admin.domain.ports.ConfigurationPersistencePort;
import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.ConfigurationRecord;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static fr.ght1pc9kc.baywatch.dsl.tables.Configuration.CONFIGURATION;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class ConfigurationPersistenceAdapter implements ConfigurationPersistencePort {
    public static final Map<String, Field<?>> CONFIGURATION_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, CONFIGURATION.CONF_ID,
            EntitiesProperties.NAME, CONFIGURATION.CONF_NAME
    );
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(CONFIGURATION_PROPERTIES_MAPPING::get);

    private final DSLContext dslContext;
    private final @DatabaseQualifier Scheduler databaseScheduler;

    private final UlidFactory ulidFactory = UlidFactory.newMonotonicInstance();

    @Override
    @SuppressWarnings("resource")
    public Flux<Entity<Entry<String, String>>> list(PageRequest pageRequest) {
        return Flux.<ConfigurationRecord>create(sink -> {
                    Condition conditions = pageRequest.filter().accept(JOOQ_CONDITION_VISITOR);
                    var query = JooqPagination.apply(
                            pageRequest.pagination(), CONFIGURATION_PROPERTIES_MAPPING,
                            dslContext.selectFrom(CONFIGURATION).where(conditions)
                    );
                    Cursor<ConfigurationRecord> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<ConfigurationRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(r -> Entity.identify(Map.entry(
                        r.getConfName(), r.getConfValue()
                )).withId(r.getConfId()));
    }

    @Override
    public Flux<Entity<Entry<String, String>>> persist(Collection<Entry<String, String>> parametersToPersist) {
        return Flux.defer(() -> dslContext.transactionResult(tx -> {
                    List<String> keys = parametersToPersist.stream()
                            .map(Entry::getKey)
                            .toList();

                    Map<String, ConfigurationRecord> existingRecords = tx.dsl().selectFrom(CONFIGURATION)
                            .where(CONFIGURATION.CONF_NAME.in(keys))
                            .fetchMap(CONFIGURATION.CONF_NAME);

                    List<ConfigurationRecord> recordsToSave = new ArrayList<>(parametersToPersist.size());
                    for (Entry<String, String> param : parametersToPersist) {
                        ConfigurationRecord configRecord = existingRecords.get(param.getKey());
                        if (configRecord == null) {
                            configRecord = tx.dsl().newRecord(CONFIGURATION);
                            configRecord.setConfId("CF" + ulidFactory.create().toString());
                            configRecord.setConfName(param.getKey());
                        }
                        configRecord.setConfValue(param.getValue());
                        recordsToSave.add(configRecord);
                    }

                    if (!recordsToSave.isEmpty()) {
                        tx.dsl().batchStore(recordsToSave).execute();
                    }

                    return Flux.fromIterable(recordsToSave);
                }))
                .subscribeOn(databaseScheduler)
                .map(r -> Entity.identify(
                                Map.entry(r.getConfName(), r.getConfValue()))
                        .withId(r.getConfId()));
    }
}
