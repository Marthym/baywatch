package fr.ght1pc9kc.baywatch.admin.domain.services;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.admin.api.model.ParameterSet;
import fr.ght1pc9kc.baywatch.admin.domain.ports.ConfigurationPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class AppConfigurationServiceImpl implements AppConfigurationService {
    private final ConfigurationPersistencePort persistencePort;

    @Override
    public Mono<ParameterSet> get(String name) {
        PageRequest request = PageRequest.all(Criteria.property("name").startWith(name));
        return persistencePort.list(request).collectList()
                .map(ParameterSet::new);
    }

    @Override
    public Mono<ParameterSet> update(Collection<Map.Entry<String, String>> configToPersist) {
        return persistencePort.persist(configToPersist).collectList()
                .map(ParameterSet::new);
    }
}
