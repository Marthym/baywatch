package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import org.intellij.lang.annotations.MagicConstant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NewsService {
    /**
     * List {@link News} for connected user or {@link fr.ght1pc9kc.baywatch.api.model.RawNews} for anonymous.
     * For Anonymous, {@link fr.ght1pc9kc.baywatch.api.model.State} is always
     * {@link fr.ght1pc9kc.baywatch.api.model.State#NONE}
     *
     * @param pageRequest {@see PageRequest}
     * @return The {@link News} for connected user or {@link fr.ght1pc9kc.baywatch.api.model.RawNews} for anonymous
     */
    Flux<News> list(PageRequest pageRequest);

    Mono<News> get(String id);

    /**
     * Set flag to a {@link News} for a {@link fr.ght1pc9kc.baywatch.api.model.User}
     *
     * @param id   The ID of the News
     * @param flag The flag to set
     * @return The News updated
     */
    Mono<News> mark(String id, @MagicConstant(flagsFromClass = Flags.class) int flag);

    /**
     * Unset flag to a {@link News} for a {@link fr.ght1pc9kc.baywatch.api.model.User}
     *
     * @param id   The ID of the News
     * @param flag The flag to unset
     * @return The News updated
     */
    Mono<News> unmark(String id, @MagicConstant(flagsFromClass = Flags.class) int flag);
}
