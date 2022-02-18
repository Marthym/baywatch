package fr.ght1pc9kc.baywatch.api.techwatch;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.techwatch.model.Flags;
import fr.ght1pc9kc.baywatch.api.techwatch.model.News;
import fr.ght1pc9kc.baywatch.api.techwatch.model.State;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.api.techwatch.model.RawNews;
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

    Mono<News> get(String id);

    /**
     * Set flag to a {@link News} for a {@link User}
     *
     * @param id   The ID of the News
     * @param flag The flag to set
     * @return The News updated
     */
    Mono<News> mark(String id, @MagicConstant(flagsFromClass = Flags.class) int flag);

    /**
     * Unset flag to a {@link News} for a {@link User}
     *
     * @param id   The ID of the News
     * @param flag The flag to unset
     * @return The News updated
     */
    Mono<News> unmark(String id, @MagicConstant(flagsFromClass = Flags.class) int flag);

    /**
     * Delete all links between {@link News} and {@link Feed}
     *
     * @param toOrphanize The {@link News} to be orphans
     * @return nothing
     */
    Mono<Integer> orphanize(Collection<String> toOrphanize);

    /**
     * Delete {@link News} from the database
     *
     * @param toDelete ID of the {@link News}s to delete
     * @return The number of {@link News} effectively deleted
     */
    Mono<Integer> delete(Collection<String> toDelete);
}
