package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import org.intellij.lang.annotations.MagicConstant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StatePersistencePort {

    Mono<Entity<State>> get(QueryContext queryContext);

    Flux<Entity<State>> list(QueryContext queryContext);

    Mono<Entity<State>> flag(
            String newsId, String userId, @MagicConstant(flagsFromClass = Flags.class) int flag);

    Mono<Entity<State>> unflag(
            String newsId, String userId, @MagicConstant(flagsFromClass = Flags.class) int flag);

}
