package fr.ght1pc9kc.baywatch.admin.api;

import fr.ght1pc9kc.baywatch.admin.api.model.ParameterSet;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map.Entry;

public interface AppConfigurationService {
    Mono<ParameterSet> get(String name);

    Mono<ParameterSet> update(Collection<Entry<String, String>> parameters);
}
