package fr.ght1pc9kc.baywatch.techwatch.api.model;

import java.util.Set;

/**
 * The popularity of a {@link News}
 *
 * @param id         The {@link News} ID
 * @param popularity the number of fans
 * @param fans       set of {@link fr.ght1pc9kc.baywatch.security.api.model.User} who share the news
 */
public record Popularity(
        String id,
        int popularity,
        Set<String> fans
) {
}
