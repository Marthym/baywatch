package fr.ght1pc9kc.baywatch.admin.domain.ports;

import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;

import java.util.Map.Entry;

public interface ConfigurationPersistencePort {
    Flux<Entity<Entry<String, String>>> list(PageRequest pageRequest);
}
