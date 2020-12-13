package fr.ght1pc9kc.baywatch.api.model.search;

@lombok.Value
public class Field {
    public String name;

    public <T> Criteria eq(T value) {
        return new EqualOperation<>(this, new Value<>(value));
    }

    public <T> Criteria gt(T value) {
        return new GreaterThanOperation<>(this, new Value<>(value));
    }

    public <T> Criteria lt(T value) {
        return new LowerThanOperation<>(this, new Value<>(value));
    }
}
