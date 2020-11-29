package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.News;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NewsPersistencePort {
    Mono<Void> create(Collection<News> toCreate);
}
