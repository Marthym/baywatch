package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.util.Arrays;
import java.util.Collection;

@Value
@Getter(AccessLevel.PACKAGE)
public class CriterionProperty {
    public String property;

    public <T> Criteria eq(T value) {
        return new EqualOperation<>(this, new CriterionValue<>(value));
    }

    public <T> Criteria in(Collection<T> values) {
        return new InOperation<>(this, new CriterionValue<>(values));
    }

    @SafeVarargs
    public final <T> Criteria in(T... values) {
        return new InOperation<>(this, new CriterionValue<>(Arrays.asList(values)));
    }

    public <T> Criteria gt(T value) {
        return new GreaterThanOperation<>(this, new CriterionValue<>(value));
    }

    public <T> Criteria lt(T value) {
        return new LowerThanOperation<>(this, new CriterionValue<>(value));
    }
}
