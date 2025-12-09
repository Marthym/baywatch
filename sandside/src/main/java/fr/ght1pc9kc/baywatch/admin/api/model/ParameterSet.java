package fr.ght1pc9kc.baywatch.admin.api.model;

import fr.ght1pc9kc.entity.api.Entity;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ParameterSet {
    private final Map<String, Entity<String>> parameters;

    public ParameterSet(Collection<Entity<Entry<String, String>>> parameters) {
        this.parameters = Map.copyOf(parameters.stream()
                .map(e -> Map.entry(e.self().getKey(), e.convert(Entry::getValue)))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue)));
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
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

    public Map<String, Object> toMap() {
        Map<String, Object> root = new java.util.HashMap<>();

        for (Entry<String, Entity<String>> entry : parameters.entrySet()) {
            String[] path = entry.getKey().split("\\.");
            Map<String, Object> current = root;

            for (int i = 0; i < path.length - 1; i++) {
                String node = path[i];
                Object value = current.get(node);

                if (!(value instanceof Map)) {
                    value = new java.util.HashMap<String, Object>();
                    current.put(node, value);
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) value;
                current = subMap;
            }

            current.put(path[path.length - 1], entry.getValue().self());
        }

        return root;
    }
}
