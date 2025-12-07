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

    public Map<String, Object> toMap(@Nullable String prefix) {
        return parameters.entrySet().stream()
                .filter(e -> isNull(prefix) || e.getKey().startsWith(prefix))
                .map(e -> Map.entry(dotToCamel(prefix, e.getKey()), e.getValue().self()))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    public String dotToCamel(@Nullable String prefix, String input) {
        String unprefixed = nonNull(prefix) ? input.substring(prefix.length() + 1) : input;
        return Arrays.stream(unprefixed.split("\\."))
                .filter(part -> !part.isEmpty())
                .reduce("", (left, right) ->
                        left + ((left.isEmpty()) ? right : Character.toUpperCase(right.charAt(0)) + right.substring(1)));
    }
}
