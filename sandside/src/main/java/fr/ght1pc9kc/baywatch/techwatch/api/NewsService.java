package fr.ght1pc9kc.baywatch.techwatch.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.intellij.lang.annotations.MagicConstant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NewsService {
    /**
     * List {@link News} for connected user or {@link RawNews} for anonymous.
     * For Anonymous, {@link State} is always
     * {@link State#NONE}
     *
     * @param pageRequest {@see PageRequest}
     * @return The {@link News} for connected user or {@link RawNews} for anonymous
     */
    Flux<News> list(PageRequest pageRequest);

    /**
     * Return the number of news available for the {@link PageRequest}
     *
     * @param pageRequest The request parameters
     * @return The total number of returned element without pagination
     */
    Mono<Integer> count(PageRequest pageRequest);

    Mono<News> get(String id);

    /**
     * Set flag to a {@link News} for a {@link User}
     *
     * @param id   The ID of the News
     * @param flag The flag to set
     * @return The updated {@link Entity<State>}
     */
    Mono<Entity<State>> mark(String id, @MagicConstant(flagsFromClass = Flags.class) int flag);

    /**
     * Unset flag to a {@link News} for a {@link User}
     *
     * @param id   The ID of the News
     * @param flag The flag to unset
     * @return The updated {@link Entity<State>}
     */
    Mono<Entity<State>> unmark(String id, @MagicConstant(flagsFromClass = Flags.class) int flag);

    /**
     * Delete {@link News} from the database
     *
     * @param toDelete ID of the {@link News}s to delete
     * @return The number of {@link News} effectively deleted
     */
    Mono<Integer> delete(Collection<String> toDelete);
}
