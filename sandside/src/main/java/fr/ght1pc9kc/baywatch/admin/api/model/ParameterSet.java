package fr.ght1pc9kc.baywatch.admin.api.model;

import fr.ght1pc9kc.entity.api.Entity;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ParameterSet {
    private final Map<String, Entity<String>> parameters;

    public ParameterSet(Collection<Entity<Entry<String, String>>> parameters) {
        this.parameters = Map.copyOf(parameters.stream()
                .map(e -> Map.entry(e.self().getKey(), e.convert(Entry::getValue)))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue)));
    }

    public String getValue(String key) {
        if (!parameters.containsKey(key)) {
            throw new IllegalArgumentException("No parameter with key " + key);
        }
        return parameters.get(key).self();
    }

    public String getId(String key) {
        if (!parameters.containsKey(key)) {
            throw new IllegalArgumentException("No parameter with key " + key);
        }
        return parameters.get(key).id();
    }
}
