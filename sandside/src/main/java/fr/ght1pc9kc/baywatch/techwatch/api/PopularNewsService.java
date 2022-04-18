package fr.ght1pc9kc.baywatch.techwatch.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Popularity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface PopularNewsService {
    /**
     * Retrieve information about {@link fr.ght1pc9kc.baywatch.techwatch.api.model.News} popularity
     * <p>
     * The popularity is the number of sharing by all other {@link fr.ght1pc9kc.baywatch.security.api.model.User}
     *
     * @param ids The {@link fr.ght1pc9kc.baywatch.techwatch.api.model.News} ids looking for
     * @return The {@link Popularity} information
     */
    Flux<Popularity> get(Collection<String> ids);
}
