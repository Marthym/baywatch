package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.juery.api.Criteria;
import org.intellij.lang.annotations.MagicConstant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NewsPersistencePort {
    Mono<News> get(QueryContext qCtx);

    default Flux<News> list() {
        return list(QueryContext.empty());
    }

    Flux<News> list(QueryContext qCtx);

    Mono<Integer> persist(Collection<News> toCreate);

    Flux<Entity<State>> listState(Criteria searchCriteria);

    Mono<Integer> addStateFlag(
            String newsId, String userId, @MagicConstant(flagsFromClass = Flags.class) int flag);

    Mono<Integer> removeStateFlag(
            String newsId, String userId, @MagicConstant(flagsFromClass = Flags.class) int flag);

    Mono<Integer> unlink(Collection<String> ids);

    Mono<Integer> delete(Collection<String> ids);

    Mono<Integer> count(QueryContext qCtx);
}
