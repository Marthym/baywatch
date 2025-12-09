package fr.ght1pc9kc.baywatch.admin.api;

import fr.ght1pc9kc.baywatch.admin.api.model.ParameterSet;
import reactor.core.publisher.Mono;

public interface AppConfigurationService {
    Mono<ParameterSet> get(String name);

    Mono<ParameterSet> update(ParameterSet configToPersist);
}
