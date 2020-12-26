package fr.ght1pc9kc.baywatch.domain.scrapper.actions;

import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.api.scrapper.PreScrappingAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurgeNewsAction implements PreScrappingAction {
    private static final int DELETE_BUFFER_SIZE = 500;

    private final NewsPersistencePort newsPersistence;
    private final Clock clock;

    @Override
    public Mono<Void> call() {
        LocalDateTime maxPublicationPasDate = LocalDateTime.now(clock).minus(Period.ofMonths(3));
        Criteria criteria = Criteria.property("publication").lt(maxPublicationPasDate);
        return newsPersistence.list(criteria)
                .map(RawNews::getId)
                .collectList()
                .flatMapMany(this::keepStaredNewsIds)
                .buffer(DELETE_BUFFER_SIZE)
                .flatMap(newsPersistence::delete)
                .onErrorContinue((t, o) -> {
                    log.error("{}: {}", t.getCause(), t.getLocalizedMessage());
                    log.debug("STACKTRACE", t);
                }).then();
    }

    private Flux<String> keepStaredNewsIds(Collection<String> newsIds) {
        Criteria isStaredCriteria = Criteria.property("newsId").in(newsIds)
                .and(Criteria.property("stared").eq(true));
        return newsPersistence.listState(isStaredCriteria)
                .map(Map.Entry::getKey)
                .collectList()
                .flatMapMany(stareds -> {
                    Collection<String> toBeDeleted = new ArrayList<>(newsIds);
                    toBeDeleted.removeAll(stareds);
                    return Flux.fromIterable(toBeDeleted);
                });
    }
}
