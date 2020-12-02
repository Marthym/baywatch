package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;

@Slf4j
@Component
@AllArgsConstructor
public class NewsRepository implements NewsPersistencePort {
    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ConversionService conversionService;

    @Override
    public Mono<Void> create(Collection<News> toCreate) {
        List<NewsRecord> records = toCreate.stream()
                .map(n -> conversionService.convert(n, NewsRecord.class))
                .collect(Collectors.toList());

        return Mono.fromCallable(() ->
                dsl.loadInto(NEWS)
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
                }).then();

    }
}
